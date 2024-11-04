import pandas as pd
import numpy as np
from sklearn.preprocessing import StandardScaler, OneHotEncoder
from sklearn.decomposition import PCA
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.pipeline import Pipeline
from sklearn.compose import ColumnTransformer
from sklearn.impute import SimpleImputer

class FeatureEngineering:
    def __init__(self, user_data_path, product_data_path):
        self.user_data_path = user_data_path
        self.product_data_path = product_data_path
        self.user_data = pd.read_csv(user_data_path)
        self.product_data = pd.read_csv(product_data_path)

    def clean_user_data(self):
        """Handle missing values and normalize user data."""
        # Fill missing values with median for numerical and most frequent for categorical
        num_cols = self.user_data.select_dtypes(include=['int64', 'float64']).columns
        cat_cols = self.user_data.select_dtypes(include=['object']).columns

        self.user_data[num_cols] = self.user_data[num_cols].fillna(self.user_data[num_cols].median())
        self.user_data[cat_cols] = self.user_data[cat_cols].fillna(self.user_data[cat_cols].mode().iloc[0])

        # Standardize numerical features
        scaler = StandardScaler()
        self.user_data[num_cols] = scaler.fit_transform(self.user_data[num_cols])

        return self.user_data

    def clean_product_data(self):
        """Handle missing values and normalize product data."""
        # Fill missing values for product data
        num_cols = self.product_data.select_dtypes(include=['int64', 'float64']).columns
        cat_cols = self.product_data.select_dtypes(include=['object']).columns

        self.product_data[num_cols] = self.product_data[num_cols].fillna(self.product_data[num_cols].median())
        self.product_data[cat_cols] = self.product_data[cat_cols].fillna(self.product_data[cat_cols].mode().iloc[0])

        # Standardize numerical features
        scaler = StandardScaler()
        self.product_data[num_cols] = scaler.fit_transform(self.product_data[num_cols])

        return self.product_data

    def extract_user_features(self):
        """Create user-specific features such as interaction history, demographics."""
        # Extracting age from date of birth
        self.user_data['age'] = 2024 - pd.to_datetime(self.user_data['date_of_birth']).dt.year

        # Encoding categorical features
        cat_cols = ['gender', 'location']
        encoder = OneHotEncoder(sparse=False, handle_unknown='ignore')
        encoded_cat = pd.DataFrame(encoder.fit_transform(self.user_data[cat_cols]), columns=encoder.get_feature_names_out(cat_cols))
        
        self.user_data = pd.concat([self.user_data, encoded_cat], axis=1).drop(cat_cols, axis=1)
        return self.user_data

    def extract_product_features(self):
        """Extract product-specific features like category, description embeddings, etc."""
        # Apply TF-IDF on product description
        tfidf_vectorizer = TfidfVectorizer(max_features=100)
        tfidf_matrix = tfidf_vectorizer.fit_transform(self.product_data['description'])

        # Converting sparse matrix to DataFrame
        tfidf_df = pd.DataFrame(tfidf_matrix.toarray(), columns=tfidf_vectorizer.get_feature_names_out())
        
        # Combine with original product data
        self.product_data = pd.concat([self.product_data, tfidf_df], axis=1)

        # Dropping original description column
        self.product_data.drop('description', axis=1, inplace=True)
        
        return self.product_data

    def apply_pca(self, n_components=10):
        """Apply PCA to reduce dimensionality of feature sets."""
        # Combine user and product data
        combined_data = pd.concat([self.user_data, self.product_data], axis=1)

        # Perform PCA for dimensionality reduction
        pca = PCA(n_components=n_components)
        pca_result = pca.fit_transform(combined_data)

        # Convert result back to DataFrame
        pca_df = pd.DataFrame(pca_result, columns=[f'pca_{i}' for i in range(n_components)])

        return pca_df

    def create_feature_pipeline(self):
        """Create a pipeline that performs all transformations automatically."""
        num_features = self.user_data.select_dtypes(include=['int64', 'float64']).columns.tolist()
        cat_features = ['gender', 'location']

        # Defining pipeline for numerical and categorical transformations
        num_transformer = Pipeline(steps=[
            ('imputer', SimpleImputer(strategy='median')),
            ('scaler', StandardScaler())
        ])

        cat_transformer = Pipeline(steps=[
            ('imputer', SimpleImputer(strategy='most_frequent')),
            ('encoder', OneHotEncoder(handle_unknown='ignore', sparse=False))
        ])

        # Combining transformers using ColumnTransformer
        preprocessor = ColumnTransformer(transformers=[
            ('num', num_transformer, num_features),
            ('cat', cat_transformer, cat_features)
        ])

        return preprocessor

    def preprocess_data(self):
        """Apply all preprocessing steps."""
        user_data_clean = self.clean_user_data()
        product_data_clean = self.clean_product_data()

        user_features = self.extract_user_features()
        product_features = self.extract_product_features()

        # Apply pipeline transformations
        preprocessor = self.create_feature_pipeline()
        user_data_transformed = preprocessor.fit_transform(user_features)
        product_data_transformed = preprocessor.fit_transform(product_features)

        return pd.DataFrame(user_data_transformed), pd.DataFrame(product_data_transformed)


if __name__ == "__main__":
    feature_engineer = FeatureEngineering("datasets/user_data.csv", "datasets/product_data.csv")

    # Preprocess user and product data
    user_data_transformed, product_data_transformed = feature_engineer.preprocess_data()

    # Apply PCA for dimensionality reduction
    final_features = feature_engineer.apply_pca()

    # Saving the processed features
    final_features.to_csv("datasets/processed_features.csv", index=False)
    print("Feature engineering completed and saved.")