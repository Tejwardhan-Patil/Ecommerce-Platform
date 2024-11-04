import axios from 'axios';
import store from '../store/reduxStore';
import { logoutUser } from '../store/userSlice';

const ApiService = {
    baseURL: process.env.REACT_APP_API_URL,

    // Configure the Axios instance
    axiosInstance: axios.create({
        baseURL: this.baseURL,
        headers: {
            'Content-Type': 'application/json',
        },
        timeout: 10000,
    }),

    // Function to set auth token
    setAuthToken(token) {
        if (token) {
            this.axiosInstance.defaults.headers.common['Authorization'] = `Bearer ${token}`;
        } else {
            delete this.axiosInstance.defaults.headers.common['Authorization'];
        }
    },

    // GET request method
    async get(endpoint, params = {}) {
        try {
            const response = await this.axiosInstance.get(endpoint, { params });
            return response.data;
        } catch (error) {
            this.handleError(error);
        }
    },

    // POST request method
    async post(endpoint, data) {
        try {
            const response = await this.axiosInstance.post(endpoint, data);
            return response.data;
        } catch (error) {
            this.handleError(error);
        }
    },

    // PUT request method
    async put(endpoint, data) {
        try {
            const response = await this.axiosInstance.put(endpoint, data);
            return response.data;
        } catch (error) {
            this.handleError(error);
        }
    },

    // DELETE request method
    async delete(endpoint) {
        try {
            const response = await this.axiosInstance.delete(endpoint);
            return response.data;
        } catch (error) {
            this.handleError(error);
        }
    },

    // Handle API errors
    handleError(error) {
        if (error.response) {
            const statusCode = error.response.status;
            if (statusCode === 401) {
                // Unauthenticated, log the user out
                store.dispatch(logoutUser());
            }
            console.error('API Error:', error.response.data);
        } else if (error.request) {
            console.error('Network Error:', error.request);
        } else {
            console.error('Error:', error.message);
        }
        throw error;
    },

    // User APIs
    async login(credentials) {
        const response = await this.post('/auth/login', credentials);
        const { token } = response;
        this.setAuthToken(token);
        return response;
    },

    async logout() {
        await this.post('/auth/logout');
        this.setAuthToken(null);
    },

    async register(userData) {
        return this.post('/auth/register', userData);
    },

    async getUserProfile() {
        return this.get('/user/profile');
    },

    // Product APIs
    async fetchProducts(params) {
        return this.get('/products', params);
    },

    async getProductById(productId) {
        return this.get(`/products/${productId}`);
    },

    async createProduct(productData) {
        return this.post('/products', productData);
    },

    async updateProduct(productId, productData) {
        return this.put(`/products/${productId}`, productData);
    },

    async deleteProduct(productId) {
        return this.delete(`/products/${productId}`);
    },

    // Cart APIs
    async fetchCart() {
        return this.get('/cart');
    },

    async addItemToCart(itemId, quantity) {
        const data = { itemId, quantity };
        return this.post('/cart/add', data);
    },

    async updateCartItem(itemId, quantity) {
        const data = { quantity };
        return this.put(`/cart/${itemId}`, data);
    },

    async removeItemFromCart(itemId) {
        return this.delete(`/cart/${itemId}`);
    },

    // Order APIs
    async placeOrder(orderData) {
        return this.post('/orders', orderData);
    },

    async getOrderById(orderId) {
        return this.get(`/orders/${orderId}`);
    },

    async fetchUserOrders() {
        return this.get('/orders/user');
    },

    // Payment APIs
    async initiatePayment(paymentData) {
        return this.post('/payments/initiate', paymentData);
    },

    async getPaymentStatus(paymentId) {
        return this.get(`/payments/${paymentId}/status`);
    },

    // Wishlist APIs
    async fetchWishlist() {
        return this.get('/wishlist');
    },

    async addItemToWishlist(itemId) {
        return this.post('/wishlist/add', { itemId });
    },

    async removeItemFromWishlist(itemId) {
        return this.delete(`/wishlist/${itemId}`);
    },

    // Notification APIs
    async fetchNotifications() {
        return this.get('/notifications');
    },

    async markNotificationAsRead(notificationId) {
        return this.put(`/notifications/${notificationId}/read`);
    },

    // Utility functions
    async uploadFile(file) {
        const formData = new FormData();
        formData.append('file', file);
        const config = {
            headers: {
                'Content-Type': 'multipart/form-data',
            },
        };
        const response = await this.axiosInstance.post('/upload', formData, config);
        return response.data;
    },

    async downloadFile(fileId) {
        const response = await this.axiosInstance.get(`/download/${fileId}`, {
            responseType: 'blob',
        });
        return response.data;
    },
};

export default ApiService;