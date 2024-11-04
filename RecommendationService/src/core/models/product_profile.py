from typing import List, Dict, Optional
from dataclasses import dataclass
import json
from datetime import datetime

@dataclass
class ProductCategory:
    id: int
    name: str
    parent_category: Optional['ProductCategory'] = None

    def to_dict(self) -> Dict:
        return {
            "id": self.id,
            "name": self.name,
            "parent_category": self.parent_category.to_dict() if self.parent_category else None
        }

@dataclass
class ProductReview:
    user_id: str
    rating: float
    comment: str
    timestamp: datetime

    def to_dict(self) -> Dict:
        return {
            "user_id": self.user_id,
            "rating": self.rating,
            "comment": self.comment,
            "timestamp": self.timestamp.isoformat()
        }

@dataclass
class ProductAttribute:
    key: str
    value: str

    def to_dict(self) -> Dict:
        return {
            "key": self.key,
            "value": self.value
        }

@dataclass
class ProductProfile:
    product_id: str
    name: str
    description: str
    price: float
    stock: int
    category: ProductCategory
    reviews: List[ProductReview]
    attributes: List[ProductAttribute]
    created_at: datetime
    updated_at: datetime
    rating: Optional[float] = None

    def calculate_average_rating(self) -> None:
        if self.reviews:
            total_rating = sum(review.rating for review in self.reviews)
            self.rating = total_rating / len(self.reviews)
        else:
            self.rating = None

    def add_review(self, review: ProductReview) -> None:
        self.reviews.append(review)
        self.updated_at = datetime.now()
        self.calculate_average_rating()

    def add_attribute(self, key: str, value: str) -> None:
        self.attributes.append(ProductAttribute(key=key, value=value))

    def to_dict(self) -> Dict:
        return {
            "product_id": self.product_id,
            "name": self.name,
            "description": self.description,
            "price": self.price,
            "stock": self.stock,
            "category": self.category.to_dict(),
            "reviews": [review.to_dict() for review in self.reviews],
            "attributes": [attribute.to_dict() for attribute in self.attributes],
            "created_at": self.created_at.isoformat(),
            "updated_at": self.updated_at.isoformat(),
            "rating": self.rating
        }

    @classmethod
    def from_dict(cls, data: Dict) -> 'ProductProfile':
        category = ProductCategory(
            id=data["category"]["id"],
            name=data["category"]["name"],
            parent_category=ProductCategory.from_dict(data["category"]["parent_category"]) if data["category"].get("parent_category") else None
        )
        reviews = [ProductReview(
            user_id=review["user_id"],
            rating=review["rating"],
            comment=review["comment"],
            timestamp=datetime.fromisoformat(review["timestamp"])
        ) for review in data["reviews"]]

        attributes = [ProductAttribute(key=attr["key"], value=attr["value"]) for attr in data["attributes"]]

        return cls(
            product_id=data["product_id"],
            name=data["name"],
            description=data["description"],
            price=data["price"],
            stock=data["stock"],
            category=category,
            reviews=reviews,
            attributes=attributes,
            created_at=datetime.fromisoformat(data["created_at"]),
            updated_at=datetime.fromisoformat(data["updated_at"]),
            rating=data.get("rating")
        )

class ProductProfileManager:
    def __init__(self):
        self.product_profiles: Dict[str, ProductProfile] = {}

    def add_product_profile(self, product_profile: ProductProfile) -> None:
        self.product_profiles[product_profile.product_id] = product_profile

    def remove_product_profile(self, product_id: str) -> None:
        if product_id in self.product_profiles:
            del self.product_profiles[product_id]

    def get_product_profile(self, product_id: str) -> Optional[ProductProfile]:
        return self.product_profiles.get(product_id)

    def update_product_stock(self, product_id: str, new_stock: int) -> None:
        product = self.get_product_profile(product_id)
        if product:
            product.stock = new_stock
            product.updated_at = datetime.now()

    def to_dict(self) -> Dict[str, Dict]:
        return {product_id: product.to_dict() for product_id, product in self.product_profiles.items()}

    def save_to_file(self, file_path: str) -> None:
        with open(file_path, 'w') as file:
            json.dump(self.to_dict(), file, indent=4)

    def load_from_file(self, file_path: str) -> None:
        with open(file_path, 'r') as file:
            data = json.load(file)
            self.product_profiles = {product_id: ProductProfile.from_dict(profile_data) for product_id, profile_data in data.items()}

    def find_products_by_category(self, category_id: int) -> List[ProductProfile]:
        return [
            product for product in self.product_profiles.values()
            if product.category.id == category_id
        ]

    def search_products_by_name(self, search_term: str) -> List[ProductProfile]:
        return [
            product for product in self.product_profiles.values()
            if search_term.lower() in product.name.lower()
        ]

# Usage
if __name__ == "__main__":
    category1 = ProductCategory(id=1, name="Electronics")
    category2 = ProductCategory(id=2, name="Laptops", parent_category=category1)

    product1 = ProductProfile(
        product_id="p001",
        name="Laptop Pro",
        description="High performance laptop",
        price=1500.0,
        stock=10,
        category=category2,
        reviews=[],
        attributes=[ProductAttribute(key="RAM", value="16GB"), ProductAttribute(key="Storage", value="512GB SSD")],
        created_at=datetime.now(),
        updated_at=datetime.now()
    )

    manager = ProductProfileManager()
    manager.add_product_profile(product1)

    manager.update_product_stock("p001", 8)
    manager.save_to_file("products.json")