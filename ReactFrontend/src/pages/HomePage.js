import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { fetchProducts } from '../store/productActions';
import ProductCard from '../components/ProductCard';
import './HomePage.scss';
import { addToCart } from '../store/cartActions';
import Banner from '../components/Banner';

const HomePage = () => {
    const dispatch = useDispatch();
    const products = useSelector((state) => state.products.items);
    const loading = useSelector((state) => state.products.loading);
    const error = useSelector((state) => state.products.error);
    const [selectedCategory, setSelectedCategory] = useState('all');
    const [searchTerm, setSearchTerm] = useState('');

    useEffect(() => {
        dispatch(fetchProducts());
    }, [dispatch]);

    const handleAddToCart = (productId) => {
        dispatch(addToCart(productId));
    };

    const filterProducts = () => {
        return products.filter((product) =>
            (selectedCategory === 'all' || product.category === selectedCategory) &&
            product.name.toLowerCase().includes(searchTerm.toLowerCase())
        );
    };

    return (
        <div className="homepage-container">
            <Banner />
            <div className="homepage-header">
                <h1>Welcome to Our Shop</h1>
                <div className="search-filter-container">
                    <input
                        type="text"
                        placeholder="Search products..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        className="search-input"
                    />
                    <select
                        value={selectedCategory}
                        onChange={(e) => setSelectedCategory(e.target.value)}
                        className="category-select"
                    >
                        <option value="all">All Categories</option>
                        <option value="electronics">Electronics</option>
                        <option value="fashion">Fashion</option>
                        <option value="home">Home</option>
                        <option value="toys">Toys</option>
                    </select>
                </div>
            </div>
            <div className="product-list">
                {loading ? (
                    <div className="loading">Loading products...</div>
                ) : error ? (
                    <div className="error">Error: {error}</div>
                ) : (
                    filterProducts().map((product) => (
                        <ProductCard
                            key={product.id}
                            product={product}
                            onAddToCart={() => handleAddToCart(product.id)}
                        />
                    ))
                )}
            </div>
            <footer className="homepage-footer">
                <div className="footer-links">
                    <Link to="/about">About Us</Link>
                    <Link to="/contact">Contact</Link>
                    <Link to="/privacy">Privacy Policy</Link>
                </div>
                <div className="footer-info">
                    <p>© 2024 Ecommerce Platform. All rights reserved.</p>
                </div>
            </footer>
        </div>
    );
};

export default HomePage;