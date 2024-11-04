import os
import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score, mean_squared_error
from sklearn.ensemble import RandomForestRegressor
import joblib
from core.models.recommendation_model import RecommendationModel
from core.data.data_preprocessing import preprocess_user_data, preprocess_product_data
from core.data.feature_engineering import extract_features
from infrastructure.persistence.database_connection import DatabaseConnection
import logging

class ModelTrainingService:
    def __init__(self):
        self.model_path = os.getenv('MODEL_PATH', 'models/recommendation_model.pkl')
        self.user_data_path = os.getenv('USER_DATA_PATH', 'data/datasets/user_data.csv')
        self.product_data_path = os.getenv('PRODUCT_DATA_PATH', 'data/datasets/product_data.csv')
        self.db_connection = DatabaseConnection()
        self.logger = logging.getLogger(__name__)
        self.logger.setLevel(logging.INFO)

    def load_data(self):
        self.logger.info("Loading user and product data")
        user_data = pd.read_csv(self.user_data_path)
        product_data = pd.read_csv(self.product_data_path)
        self.logger.info(f"User data loaded with {len(user_data)} records")
        self.logger.info(f"Product data loaded with {len(product_data)} records")
        return user_data, product_data

    def preprocess_data(self, user_data, product_data):
        self.logger.info("Preprocessing user and product data")
        preprocessed_user_data = preprocess_user_data(user_data)
        preprocessed_product_data = preprocess_product_data(product_data)
        self.logger.info("Preprocessing complete")
        return preprocessed_user_data, preprocessed_product_data

    def feature_engineering(self, user_data, product_data):
        self.logger.info("Extracting features from user and product data")
        features, labels = extract_features(user_data, product_data)
        self.logger.info(f"Extracted {len(features)} features")
        return features, labels

    def split_data(self, features, labels):
        self.logger.info("Splitting data into training and testing sets")
        X_train, X_test, y_train, y_test = train_test_split(features, labels, test_size=0.2, random_state=42)
        self.logger.info(f"Training set size: {len(X_train)}")
        self.logger.info(f"Testing set size: {len(X_test)}")
        return X_train, X_test, y_train, y_test

    def train_model(self, X_train, y_train):
        self.logger.info("Training the recommendation model")
        model = RandomForestRegressor(n_estimators=100, random_state=42)
        model.fit(X_train, y_train)
        self.logger.info("Model training complete")
        return model

    def evaluate_model(self, model, X_test, y_test):
        self.logger.info("Evaluating the recommendation model")
        predictions = model.predict(X_test)
        mse = mean_squared_error(y_test, predictions)
        self.logger.info(f"Mean Squared Error: {mse}")
        return mse

    def save_model(self, model):
        self.logger.info(f"Saving the trained model to {self.model_path}")
        joblib.dump(model, self.model_path)
        self.logger.info("Model saved successfully")

    def load_model(self):
        if os.path.exists(self.model_path):
            self.logger.info(f"Loading the trained model from {self.model_path}")
            model = joblib.load(self.model_path)
            self.logger.info("Model loaded successfully")
            return model
        else:
            self.logger.warning("No pre-trained model found")
            return None

    def run_training_pipeline(self):
        self.logger.info("Starting the model training pipeline")

        # Load data
        user_data, product_data = self.load_data()

        # Preprocess data
        preprocessed_user_data, preprocessed_product_data = self.preprocess_data(user_data, product_data)

        # Feature engineering
        features, labels = self.feature_engineering(preprocessed_user_data, preprocessed_product_data)

        # Split data
        X_train, X_test, y_train, y_test = self.split_data(features, labels)

        # Train the model
        model = self.train_model(X_train, y_train)

        # Evaluate the model
        mse = self.evaluate_model(model, X_test, y_test)

        # Save the model
        self.save_model(model)

        self.logger.info("Model training pipeline complete")

    def update_model(self):
        self.logger.info("Updating the model with new data")
        model = self.load_model()

        if not model:
            self.logger.info("No existing model found, training a new model")
            self.run_training_pipeline()
        else:
            self.logger.info("Training model on new data")
            user_data, product_data = self.load_data()
            preprocessed_user_data, preprocessed_product_data = self.preprocess_data(user_data, product_data)
            features, labels = self.feature_engineering(preprocessed_user_data, preprocessed_product_data)
            X_train, X_test, y_train, y_test = self.split_data(features, labels)
            model.fit(X_train, y_train)
            mse = self.evaluate_model(model, X_test, y_test)
            self.save_model(model)
            self.logger.info("Model updated successfully")

if __name__ == "__main__":
    logging.basicConfig(level=logging.INFO)
    service = ModelTrainingService()
    service.run_training_pipeline()