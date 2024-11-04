import numpy as np
from sklearn.metrics.pairwise import cosine_similarity
from src.core.models.recommendation_model import RecommendationModel
from src.core.models.user_profile import UserProfile
from src.core.models.product_profile import ProductProfile
from src.infrastructure.persistence.database_connection import DatabaseConnection

class RecommendationService:
    def __init__(self):
        self.db = DatabaseConnection()
        self.model = RecommendationModel()

    def get_user_profile(self, user_id):
        """Fetch user profile data from the database."""
        query = "SELECT * FROM user_profiles WHERE user_id = %s"
        user_data = self.db.execute_query(query, (user_id,))
        if user_data:
            return UserProfile(**user_data[0])
        return None

    def get_product_profiles(self, product_ids):
        """Fetch product profile data from the database."""
        query = "SELECT * FROM product_profiles WHERE product_id IN %s"
        product_data = self.db.execute_query(query, (tuple(product_ids),))
        return [ProductProfile(**data) for data in product_data]

    def recommend_products_for_user(self, user_id, top_n=5):
        """Recommend top N products for a user based on their profile."""
        user_profile = self.get_user_profile(user_id)
        if not user_profile:
            return []

        # Retrieve all available product profiles
        product_profiles = self.get_all_product_profiles()

        # Compute similarity between the user profile and product profiles
        recommendations = self.compute_recommendations(user_profile, product_profiles)
        
        # Sort products by similarity score and return top N
        recommendations = sorted(recommendations, key=lambda x: x['similarity'], reverse=True)
        return recommendations[:top_n]

    def get_all_product_profiles(self):
        """Retrieve all product profiles from the database."""
        query = "SELECT * FROM product_profiles"
        product_data = self.db.execute_query(query)
        return [ProductProfile(**data) for data in product_data]

    def compute_recommendations(self, user_profile, product_profiles):
        """Compute similarity scores between the user and available products."""
        recommendations = []
        user_vector = np.array(user_profile.to_vector()).reshape(1, -1)

        for product in product_profiles:
            product_vector = np.array(product.to_vector()).reshape(1, -1)
            similarity = cosine_similarity(user_vector, product_vector)[0][0]
            recommendations.append({
                'product_id': product.product_id,
                'similarity': similarity
            })
        return recommendations

    def recommend_similar_products(self, product_id, top_n=5):
        """Recommend products similar to a given product."""
        product_profile = self.get_product_profile(product_id)
        if not product_profile:
            return []

        # Retrieve all available product profiles
        product_profiles = self.get_all_product_profiles()

        # Compute similarity between the given product and all others
        recommendations = self.compute_product_similarity(product_profile, product_profiles)
        
        # Sort products by similarity score and return top N
        recommendations = sorted(recommendations, key=lambda x: x['similarity'], reverse=True)
        return recommendations[:top_n]

    def get_product_profile(self, product_id):
        """Fetch a product profile from the database."""
        query = "SELECT * FROM product_profiles WHERE product_id = %s"
        product_data = self.db.execute_query(query, (product_id,))
        if product_data:
            return ProductProfile(**product_data[0])
        return None

    def compute_product_similarity(self, product_profile, product_profiles):
        """Compute similarity scores between a product and all other products."""
        recommendations = []
        product_vector = np.array(product_profile.to_vector()).reshape(1, -1)

        for product in product_profiles:
            if product.product_id != product_profile.product_id:
                product_vector_2 = np.array(product.to_vector()).reshape(1, -1)
                similarity = cosine_similarity(product_vector, product_vector_2)[0][0]
                recommendations.append({
                    'product_id': product.product_id,
                    'similarity': similarity
                })
        return recommendations

    def recommend_trending_products(self, top_n=5):
        """Recommend trending products based on popularity data."""
        trending_products = self.get_trending_products()

        # Fetch product profiles for trending products
        product_profiles = self.get_product_profiles([p['product_id'] for p in trending_products])

        # Sort by popularity and return top N
        trending_products_sorted = sorted(trending_products, key=lambda x: x['popularity'], reverse=True)
        return trending_products_sorted[:top_n]

    def get_trending_products(self):
        """Fetch trending products based on recent purchase or view data."""
        query = """
        SELECT product_id, COUNT(*) as popularity 
        FROM user_activity 
        WHERE event_type IN ('view', 'purchase') 
        GROUP BY product_id
        ORDER BY popularity DESC
        """
        return self.db.execute_query(query)

    def recommend_for_segment(self, segment_name, top_n=5):
        """Recommend products for a specific user segment."""
        segment_users = self.get_users_in_segment(segment_name)
        
        # Fetch product recommendations for each user in the segment
        recommendations = []
        for user in segment_users:
            user_recommendations = self.recommend_products_for_user(user['user_id'], top_n=top_n)
            recommendations.extend(user_recommendations)

        # Aggregate and sort by frequency or average similarity score
        recommendations = self.aggregate_recommendations(recommendations)
        return recommendations[:top_n]

    def get_users_in_segment(self, segment_name):
        """Fetch users that belong to a specific segment."""
        query = "SELECT user_id FROM user_segments WHERE segment_name = %s"
        return self.db.execute_query(query, (segment_name,))

    def aggregate_recommendations(self, recommendations):
        """Aggregate product recommendations across multiple users."""
        aggregated = {}
        for recommendation in recommendations:
            product_id = recommendation['product_id']
            if product_id in aggregated:
                aggregated[product_id]['count'] += 1
                aggregated[product_id]['similarity'] += recommendation['similarity']
            else:
                aggregated[product_id] = {
                    'product_id': product_id,
                    'count': 1,
                    'similarity': recommendation['similarity']
                }
        for product in aggregated.values():
            product['similarity'] /= product['count']
        return sorted(aggregated.values(), key=lambda x: (x['count'], x['similarity']), reverse=True)