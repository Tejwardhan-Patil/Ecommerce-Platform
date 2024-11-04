import datetime
from typing import List, Dict, Optional
from pydantic import BaseModel, Field
from uuid import UUID, uuid4

class UserInteraction(BaseModel):
    product_id: UUID
    interaction_type: str 
    interaction_timestamp: datetime.datetime

class UserProfile(BaseModel):
    """
    Class representing a user's profile for the recommendation system.
    It tracks user behavior, preferences, and interactions with products.
    """
    user_id: UUID = Field(default_factory=uuid4, description="Unique identifier for the user.")
    name: str
    email: str
    signup_date: datetime.date
    is_premium_user: bool = Field(default=False, description="Indicates if the user is a premium subscriber.")
    location: str = Field(default="Unknown", description="Location of the user.")
    
    # A dictionary holding user preferences for different product categories
    preferences: Dict[str, float] = Field(default_factory=dict, description="User preferences for product categories.")
    
    # List of user interactions with products
    interactions: List[UserInteraction] = Field(default_factory=list, description="User interactions with various products.")
    
    last_login: Optional[datetime.datetime] = None
    
    def update_preferences(self, category: str, rating: float):
        """
        Updates the user's preference for a given product category.
        """
        if 0 <= rating <= 1:
            self.preferences[category] = rating
        else:
            raise ValueError("Rating must be between 0 and 1.")

    def add_interaction(self, product_id: UUID, interaction_type: str):
        """
        Adds an interaction the user has had with a product.
        """
        interaction = UserInteraction(
            product_id=product_id,
            interaction_type=interaction_type,
            interaction_timestamp=datetime.datetime.now()
        )
        self.interactions.append(interaction)

    def get_recent_interactions(self, days: int = 30) -> List[UserInteraction]:
        """
        Retrieves a list of interactions the user has had within the last specified number of days.
        """
        cutoff_date = datetime.datetime.now() - datetime.timedelta(days=days)
        return [interaction for interaction in self.interactions if interaction.interaction_timestamp >= cutoff_date]
    
    def is_active(self) -> bool:
        """
        Determines whether the user is active based on their last login date and recent interactions.
        """
        if self.last_login:
            days_since_last_login = (datetime.datetime.now() - self.last_login).days
            return days_since_last_login < 30
        return False
    
    def get_preference_score(self, category: str) -> float:
        """
        Retrieves the user's preference score for a specific product category.
        If no preference exists for the category, return a neutral score (0.5).
        """
        return self.preferences.get(category, 0.5)
    
    def to_dict(self) -> Dict:
        """
        Converts the user profile to a dictionary representation, suitable for serialization.
        """
        return {
            "user_id": str(self.user_id),
            "name": self.name,
            "email": self.email,
            "signup_date": self.signup_date.isoformat(),
            "is_premium_user": self.is_premium_user,
            "location": self.location,
            "preferences": self.preferences,
            "interactions": [interaction.dict() for interaction in self.interactions],
            "last_login": self.last_login.isoformat() if self.last_login else None
        }

    def from_dict(self, data: Dict):
        """
        Updates the user profile instance from a dictionary representation.
        """
        self.user_id = UUID(data["user_id"])
        self.name = data["name"]
        self.email = data["email"]
        self.signup_date = datetime.date.fromisoformat(data["signup_date"])
        self.is_premium_user = data["is_premium_user"]
        self.location = data["location"]
        self.preferences = data["preferences"]
        self.interactions = [UserInteraction(**interaction) for interaction in data["interactions"]]
        self.last_login = datetime.datetime.fromisoformat(data["last_login"]) if data["last_login"] else None

    def recommend_based_on_preferences(self, product_catalog: List[Dict], top_n: int = 5) -> List[Dict]:
        """
        Generates product recommendations for the user based on their preferences.
        Sorts products by matching categories and preference scores.
        """
        ranked_products = []
        
        for product in product_catalog:
            category = product["category"]
            preference_score = self.get_preference_score(category)
            ranked_products.append({
                "product_id": product["product_id"],
                "category": category,
                "score": preference_score,
                "product_name": product["name"]
            })
        
        # Sort products by preference score in descending order
        ranked_products.sort(key=lambda x: x["score"], reverse=True)
        
        # Return the top N products
        return ranked_products[:top_n]

# Usage
if __name__ == "__main__":
    # Create a user profile
    user = UserProfile(
        name="Person1",
        email="person1@website.com",
        signup_date=datetime.date.today(),
        is_premium_user=True,
        location="New York"
    )
    
    # Update preferences and add some interactions
    user.update_preferences("Electronics", 0.9)
    user.update_preferences("Books", 0.7)
    
    user.add_interaction(product_id=uuid4(), interaction_type="view")
    user.add_interaction(product_id=uuid4(), interaction_type="purchase")
    
    # Product catalog
    product_catalog = [
        {"product_id": uuid4(), "category": "Electronics", "name": "Smartphone"},
        {"product_id": uuid4(), "category": "Books", "name": "Python Programming Book"},
        {"product_id": uuid4(), "category": "Clothing", "name": "Jacket"}
    ]
    
    # Generate recommendations
    recommendations = user.recommend_based_on_preferences(product_catalog, top_n=3)
    
    for rec in recommendations:
        print(f"Recommended Product: {rec['product_name']} (Score: {rec['score']})")