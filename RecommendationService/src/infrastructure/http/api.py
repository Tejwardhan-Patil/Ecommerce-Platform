from fastapi import FastAPI, HTTPException, Depends
from fastapi.responses import JSONResponse
from pydantic import BaseModel
from typing import List
from src.core.services.recommendation_service import RecommendationService
from src.core.models.user_profile import UserProfile
from src.core.models.product_profile import ProductProfile
from src.infrastructure.persistence.database_connection import DatabaseConnection

app = FastAPI()

# Initialize services and database connection
db = DatabaseConnection()
recommendation_service = RecommendationService(db)

# Request models for creating or updating user/product profiles
class UserProfileRequest(BaseModel):
    user_id: int
    name: str
    age: int
    preferences: List[str]

class ProductProfileRequest(BaseModel):
    product_id: int
    name: str
    category: str
    price: float

# Response models for recommendations
class ProductRecommendationResponse(BaseModel):
    product_id: int
    product_name: str
    score: float

# Health check endpoint
@app.get("/health", status_code=200)
def health_check():
    return {"status": "OK"}

# Get recommendations for a user
@app.get("/recommendations/{user_id}", response_model=List[ProductRecommendationResponse])
def get_recommendations(user_id: int):
    try:
        # Fetch user profile from the database
        user_profile = recommendation_service.get_user_profile(user_id)
        if not user_profile:
            raise HTTPException(status_code=404, detail="User profile not found")

        # Get product recommendations based on the user profile
        recommendations = recommendation_service.get_recommendations_for_user(user_profile)
        return [
            ProductRecommendationResponse(product_id=rec['product_id'],
                                          product_name=rec['product_name'],
                                          score=rec['score'])
            for rec in recommendations
        ]
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# Create or update a user profile
@app.post("/user-profile/", response_model=UserProfileRequest)
def create_or_update_user_profile(user_profile: UserProfileRequest):
    try:
        user_profile_data = UserProfile(
            user_id=user_profile.user_id,
            name=user_profile.name,
            age=user_profile.age,
            preferences=user_profile.preferences
        )
        recommendation_service.create_or_update_user_profile(user_profile_data)
        return user_profile
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# Create or update a product profile
@app.post("/product-profile/", response_model=ProductProfileRequest)
def create_or_update_product_profile(product_profile: ProductProfileRequest):
    try:
        product_profile_data = ProductProfile(
            product_id=product_profile.product_id,
            name=product_profile.name,
            category=product_profile.category,
            price=product_profile.price
        )
        recommendation_service.create_or_update_product_profile(product_profile_data)
        return product_profile
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# Get all user profiles
@app.get("/user-profiles/", response_model=List[UserProfileRequest])
def get_all_user_profiles():
    try:
        user_profiles = recommendation_service.get_all_user_profiles()
        return [
            UserProfileRequest(
                user_id=user.user_id,
                name=user.name,
                age=user.age,
                preferences=user.preferences
            )
            for user in user_profiles
        ]
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# Get all product profiles
@app.get("/product-profiles/", response_model=List[ProductProfileRequest])
def get_all_product_profiles():
    try:
        product_profiles = recommendation_service.get_all_product_profiles()
        return [
            ProductProfileRequest(
                product_id=product.product_id,
                name=product.name,
                category=product.category,
                price=product.price
            )
            for product in product_profiles
        ]
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# Delete a user profile
@app.delete("/user-profile/{user_id}", status_code=204)
def delete_user_profile(user_id: int):
    try:
        recommendation_service.delete_user_profile(user_id)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# Delete a product profile
@app.delete("/product-profile/{product_id}", status_code=204)
def delete_product_profile(product_id: int):
    try:
        recommendation_service.delete_product_profile(product_id)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# Endpoint for re-training the recommendation model
@app.post("/retrain-model/", status_code=202)
def retrain_model():
    try:
        recommendation_service.retrain_model()
        return {"message": "Model re-training initiated"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# Update user preferences
class UserPreferencesUpdateRequest(BaseModel):
    preferences: List[str]

@app.patch("/user-profile/{user_id}/preferences", response_model=UserProfileRequest)
def update_user_preferences(user_id: int, preferences_update: UserPreferencesUpdateRequest):
    try:
        updated_profile = recommendation_service.update_user_preferences(user_id, preferences_update.preferences)
        return UserProfileRequest(
            user_id=updated_profile.user_id,
            name=updated_profile.name,
            age=updated_profile.age,
            preferences=updated_profile.preferences
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# Handle cases where the client sends an unsupported route
@app.exception_handler(404)
async def not_found_handler(request, exc):
    return JSONResponse(status_code=404, content={"message": "Resource not found"})

# Handle any internal server errors
@app.exception_handler(500)
async def internal_server_error_handler(request, exc):
    return JSONResponse(status_code=500, content={"message": "Internal server error"})