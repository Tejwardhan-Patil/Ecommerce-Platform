import pandas as pd
import numpy as np
from sklearn.preprocessing import StandardScaler, MinMaxScaler, LabelEncoder
from sklearn.model_selection import train_test_split
from sklearn.impute import SimpleImputer
from sklearn.feature_extraction.text import TfidfVectorizer
from datetime import datetime
import os

class DataPreprocessing:
    
    def __init__(self, user_data_path, product_data_path, output_dir):
        self.user_data_path = user_data_path
        self.product_data_path = product_data_path
        self.output_dir = output_dir
        self.user_data = None
        self.product_data = None
    
    def load_data(self):
        self.user_data = pd.read_csv(self.user_data_path)
        self.product_data = pd.read_csv(self.product_data_path)
        print(f"User data loaded with {self.user_data.shape[0]} rows and {self.user_data.shape[1]} columns.")
        print(f"Product data loaded with {self.product_data.shape[0]} rows and {self.product_data.shape[1]} columns.")
    
    # Basic data cleaning
    def clean_user_data(self):
        self.user_data.drop_duplicates(inplace=True)
        self.user_data.dropna(subset=['user_id'], inplace=True)
        print("Cleaned user data:")
        print(self.user_data.head())

    def clean_product_data(self):
        self.product_data.drop_duplicates(inplace=True)
        self.product_data.dropna(subset=['product_id'], inplace=True)
        print("Cleaned product data:")
        print(self.product_data.head())
    
    # Preprocessing steps for user data
    def preprocess_user_data(self):
        # Handle missing values in age by replacing them with the median
        imputer = SimpleImputer(strategy='median')
        self.user_data['age'] = imputer.fit_transform(self.user_data[['age']])
        
        # Label encode gender column
        gender_encoder = LabelEncoder()
        self.user_data['gender'] = gender_encoder.fit_transform(self.user_data['gender'].fillna('Unknown'))
        
        # Convert registration date to datetime
        self.user_data['registration_date'] = pd.to_datetime(self.user_data['registration_date'])
        self.user_data['membership_duration'] = (datetime.now() - self.user_data['registration_date']).dt.days

        # Handle categorical data such as 'user_location'
        self.user_data = pd.get_dummies(self.user_data, columns=['user_location'], drop_first=True)
        print("Preprocessed user data:")
        print(self.user_data.head())
    
    # Preprocessing steps for product data
    def preprocess_product_data(self):
        # Fill missing values in price with mean
        price_imputer = SimpleImputer(strategy='mean')
        self.product_data['price'] = price_imputer.fit_transform(self.product_data[['price']])
        
        # Log transformation for price to reduce skewness
        self.product_data['log_price'] = np.log1p(self.product_data['price'])
        
        # Process text data in product descriptions using TF-IDF
        tfidf = TfidfVectorizer(max_features=500, stop_words='english')
        self.product_data['product_description'] = self.product_data['product_description'].fillna('')
        tfidf_matrix = tfidf.fit_transform(self.product_data['product_description'])
        tfidf_df = pd.DataFrame(tfidf_matrix.toarray(), columns=tfidf.get_feature_names_out())
        self.product_data = pd.concat([self.product_data, tfidf_df], axis=1)
        
        # Label encode product categories
        category_encoder = LabelEncoder()
        self.product_data['category'] = category_encoder.fit_transform(self.product_data['category'].fillna('Unknown'))
        
        print("Preprocessed product data:")
        print(self.product_data.head())
    
    # Feature scaling using StandardScaler
    def scale_features(self):
        scaler = StandardScaler()
        
        # Scale numerical columns in user data
        numerical_features_user = ['age', 'membership_duration']
        self.user_data[numerical_features_user] = scaler.fit_transform(self.user_data[numerical_features_user])
        
        # Scale numerical columns in product data
        numerical_features_product = ['log_price']
        self.product_data[numerical_features_product] = scaler.fit_transform(self.product_data[numerical_features_product])
        
        print("Scaled user data:")
        print(self.user_data.head())
        
        print("Scaled product data:")
        print(self.product_data.head())
    
    # Splitting data into training and testing sets
    def split_data(self):
        X_user = self.user_data.drop(columns=['user_id', 'registration_date'])
        y_user = self.user_data['user_id']
        
        X_product = self.product_data.drop(columns=['product_id', 'product_description', 'price'])
        y_product = self.product_data['product_id']
        
        X_train_user, X_test_user, y_train_user, y_test_user = train_test_split(X_user, y_user, test_size=0.2, random_state=42)
        X_train_product, X_test_product, y_train_product, y_test_product = train_test_split(X_product, y_product, test_size=0.2, random_state=42)
        
        print(f"Training user data shape: {X_train_user.shape}")
        print(f"Testing user data shape: {X_test_user.shape}")
        
        print(f"Training product data shape: {X_train_product.shape}")
        print(f"Testing product data shape: {X_test_product.shape}")
        
        return (X_train_user, X_test_user, y_train_user, y_test_user), (X_train_product, X_test_product, y_train_product, y_test_product)
    
    # Saving the processed datasets
    def save_data(self, X_train_user, X_test_user, y_train_user, y_test_user, X_train_product, X_test_product, y_train_product, y_test_product):
        if not os.path.exists(self.output_dir):
            os.makedirs(self.output_dir)
        
        # Save user data
        X_train_user.to_csv(os.path.join(self.output_dir, 'X_train_user.csv'), index=False)
        X_test_user.to_csv(os.path.join(self.output_dir, 'X_test_user.csv'), index=False)
        pd.DataFrame(y_train_user).to_csv(os.path.join(self.output_dir, 'y_train_user.csv'), index=False)
        pd.DataFrame(y_test_user).to_csv(os.path.join(self.output_dir, 'y_test_user.csv'), index=False)
        
        # Save product data
        X_train_product.to_csv(os.path.join(self.output_dir, 'X_train_product.csv'), index=False)
        X_test_product.to_csv(os.path.join(self.output_dir, 'X_test_product.csv'), index=False)
        pd.DataFrame(y_train_product).to_csv(os.path.join(self.output_dir, 'y_train_product.csv'), index=False)
        pd.DataFrame(y_test_product).to_csv(os.path.join(self.output_dir, 'y_test_product.csv'), index=False)
        
        print(f"Data saved to {self.output_dir}")
    
    # Function to preprocess, split, and save the final cleaned and processed datasets
    def execute_preprocessing(self):
        self.load_data()
        self.clean_user_data()
        self.clean_product_data()
        self.preprocess_user_data()
        self.preprocess_product_data()
        self.scale_features()
        (X_train_user, X_test_user, y_train_user, y_test_user), (X_train_product, X_test_product, y_train_product, y_test_product) = self.split_data()
        self.save_data(X_train_user, X_test_user, y_train_user, y_test_user, X_train_product, X_test_product, y_train_product, y_test_product)

# Main code execution
if __name__ == "__main__":
    data_preprocessor = DataPreprocessing(user_data_path="datasets/user_data.csv", product_data_path="datasets/product_data.csv", output_dir="processed_data")
    data_preprocessor.execute_preprocessing()