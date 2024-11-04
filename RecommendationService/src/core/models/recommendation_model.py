import numpy as np
import pandas as pd
from sklearn.metrics import mean_squared_error
from sklearn.model_selection import train_test_split
import pickle

class RecommendationModel:
    def __init__(self, num_factors=10, num_epochs=20, learning_rate=0.01, regularization=0.1):
        self.num_factors = num_factors
        self.num_epochs = num_epochs
        self.learning_rate = learning_rate
        self.regularization = regularization
        self.user_factors = None
        self.item_factors = None
        self.user_bias = None
        self.item_bias = None
        self.global_bias = None
        self.user_mapping = {}
        self.item_mapping = {}

    def fit(self, user_item_matrix):
        num_users, num_items = user_item_matrix.shape

        # Initialize factors and biases
        self.user_factors = np.random.normal(0, 0.1, (num_users, self.num_factors))
        self.item_factors = np.random.normal(0, 0.1, (num_items, self.num_factors))
        self.user_bias = np.zeros(num_users)
        self.item_bias = np.zeros(num_items)
        self.global_bias = np.mean(user_item_matrix[np.where(user_item_matrix != 0)])

        # Gradient descent to minimize the cost function
        for epoch in range(self.num_epochs):
            for user in range(num_users):
                for item in range(num_items):
                    if user_item_matrix[user, item] > 0:  # Only non-zero entries
                        prediction = self._predict_single(user, item)
                        error = user_item_matrix[user, item] - prediction
                        # Update biases
                        self.user_bias[user] += self.learning_rate * (error - self.regularization * self.user_bias[user])
                        self.item_bias[item] += self.learning_rate * (error - self.regularization * self.item_bias[item])
                        # Update latent factors
                        self.user_factors[user, :] += self.learning_rate * (
                            error * self.item_factors[item, :] - self.regularization * self.user_factors[user, :]
                        )
                        self.item_factors[item, :] += self.learning_rate * (
                            error * self.user_factors[user, :] - self.regularization * self.item_factors[item, :]
                        )

            # Log training progress
            train_rmse = self._compute_rmse(user_item_matrix)
            print(f"Epoch: {epoch + 1}, Train RMSE: {train_rmse}")

    def _predict_single(self, user, item):
        """Predict the rating of a single user-item pair."""
        prediction = self.global_bias + self.user_bias[user] + self.item_bias[item]
        prediction += np.dot(self.user_factors[user, :], self.item_factors[item, :])
        return prediction

    def predict(self, user_id, item_id):
        """Predict rating for a given user and item."""
        user = self.user_mapping.get(user_id)
        item = self.item_mapping.get(item_id)

        if user is None or item is None:
            raise ValueError("User ID or Item ID not found in training data.")
        
        return self._predict_single(user, item)

    def recommend(self, user_id, top_n=10):
        """Generate top N recommendations for a given user."""
        user = self.user_mapping.get(user_id)
        if user is None:
            raise ValueError("User ID not found in training data.")

        predictions = [
            (item, self._predict_single(user, item))
            for item in range(self.item_factors.shape[0])
        ]
        predictions.sort(key=lambda x: x[1], reverse=True)
        return predictions[:top_n]

    def _compute_rmse(self, user_item_matrix):
        """Compute RMSE on the training data."""
        predictions = []
        actuals = []
        num_users, num_items = user_item_matrix.shape
        for user in range(num_users):
            for item in range(num_items):
                if user_item_matrix[user, item] > 0:
                    predictions.append(self._predict_single(user, item))
                    actuals.append(user_item_matrix[user, item])
        return np.sqrt(mean_squared_error(actuals, predictions))

    def save_model(self, path):
        """Save the trained model to disk."""
        with open(path, 'wb') as file:
            pickle.dump(self, file)

    @staticmethod
    def load_model(path):
        """Load a saved model from disk."""
        with open(path, 'rb') as file:
            model = pickle.load(file)
        return model

    def preprocess_data(self, data, user_col, item_col, rating_col):
        """Preprocess the raw data into a user-item matrix."""
        self.user_mapping = {user_id: index for index, user_id in enumerate(data[user_col].unique())}
        self.item_mapping = {item_id: index for index, item_id in enumerate(data[item_col].unique())}

        user_item_matrix = np.zeros((len(self.user_mapping), len(self.item_mapping)))

        for row in data.itertuples():
            user = self.user_mapping[getattr(row, user_col)]
            item = self.item_mapping[getattr(row, item_col)]
            rating = getattr(row, rating_col)
            user_item_matrix[user, item] = rating

        return user_item_matrix

if __name__ == "__main__":
    # Usage
    data_path = 'src/data/datasets/user_data.csv'
    data = pd.read_csv(data_path)

    # Preprocess data
    recommendation_model = RecommendationModel(num_factors=15, num_epochs=25, learning_rate=0.005, regularization=0.02)
    user_item_matrix = recommendation_model.preprocess_data(data, user_col='user_id', item_col='product_id', rating_col='rating')

    # Train the model
    recommendation_model.fit(user_item_matrix)

    # Generate recommendations for a user
    user_id = 1
    recommendations = recommendation_model.recommend(user_id, top_n=5)
    print(f"Top recommendations for User {user_id}: {recommendations}")

    # Save the model
    model_path = 'src/core/models/recommendation_model.pkl'
    recommendation_model.save_model(model_path)