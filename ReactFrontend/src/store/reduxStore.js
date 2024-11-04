import { configureStore, getDefaultMiddleware } from '@reduxjs/toolkit';
import userReducer from './slices/userSlice';
import productReducer from './slices/productSlice';
import cartReducer from './slices/cartSlice';
import thunk from 'redux-thunk';
import logger from 'redux-logger';
import { apiMiddleware } from '../services/ApiService';

// Define Middleware
const middleware = [...getDefaultMiddleware(), thunk, apiMiddleware];

// Add logger in development mode
if (process.env.NODE_ENV === 'development') {
    middleware.push(logger);
}

// Configure the Redux Store
const store = configureStore({
    reducer: {
        user: userReducer,
        product: productReducer,
        cart: cartReducer
    },
    middleware: (getDefaultMiddleware) => getDefaultMiddleware().concat(middleware),
    devTools: process.env.NODE_ENV !== 'production'
});

// Export store
export default store;

// Code to handle hot reloading of reducers
if (process.env.NODE_ENV === 'development' && module.hot) {
    module.hot.accept('./slices', () => {
        const newUserReducer = require('./slices/userSlice').default;
        const newProductReducer = require('./slices/productSlice').default;
        const newCartReducer = require('./slices/cartSlice').default;

        store.replaceReducer({
            user: newUserReducer,
            product: newProductReducer,
            cart: newCartReducer
        });
    });
}

// Subscriber for listening to store changes
store.subscribe(() => {
    const state = store.getState();
    console.log('Store state updated: ', state);
});

// Custom middleware to log actions
const customMiddleware = (storeAPI) => (next) => (action) => {
    console.log('Dispatching action: ', action);
    const result = next(action);
    console.log('Next state: ', storeAPI.getState());
    return result;
};

// Reconfigure store with custom middleware
const updatedStore = configureStore({
    reducer: {
        user: userReducer,
        product: productReducer,
        cart: cartReducer
    },
    middleware: (getDefaultMiddleware) =>
        getDefaultMiddleware().concat(customMiddleware, thunk, apiMiddleware),
    devTools: process.env.NODE_ENV !== 'production'
});

// Code to dynamically add a new slice reducer
function injectReducer(key, asyncReducer) {
    store.asyncReducers[key] = asyncReducer;
    store.replaceReducer({
        ...store.reducer,
        [key]: asyncReducer
    });
}

// Code to dynamically remove a reducer
function removeReducer(key) {
    const { [key]: removed, ...restReducers } = store.reducer;
    store.replaceReducer(restReducers);
}

// Middleware to handle async API calls
const apiHandlerMiddleware = (storeAPI) => (next) => (action) => {
    if (action.type.includes('/pending')) {
        console.log(`API Request started: ${action.type}`);
    } else if (action.type.includes('/fulfilled')) {
        console.log(`API Request succeeded: ${action.type}`);
    } else if (action.type.includes('/rejected')) {
        console.error(`API Request failed: ${action.type}`);
    }
    return next(action);
};

// Applying the new middleware to store
const finalStore = configureStore({
    reducer: {
        user: userReducer,
        product: productReducer,
        cart: cartReducer
    },
    middleware: (getDefaultMiddleware) =>
        getDefaultMiddleware().concat(apiHandlerMiddleware, thunk, apiMiddleware),
    devTools: process.env.NODE_ENV !== 'production'
});

// Usage of the store dispatch method to dispatch an action
store.dispatch({
    type: 'user/fetchUserData',
    payload: { id: 123 }
});

// Dispatch an action to update the store state
store.dispatch({
    type: 'cart/addToCart',
    payload: {
        id: 1,
        name: 'Product 1',
        price: 100
    }
});

// Dispatch an action to remove an item from the cart
store.dispatch({
    type: 'cart/removeFromCart',
    payload: 1
});

// Function to reset store's state
function resetStoreState() {
    store.dispatch({ type: 'RESET_STORE' });
}

// Set up store persistence
function saveState(state) {
    try {
        const serializedState = JSON.stringify(state);
        localStorage.setItem('storeState', serializedState);
    } catch (err) {
        console.error('Failed to save state', err);
    }
}

// Function to load state from localStorage
function loadState() {
    try {
        const serializedState = localStorage.getItem('storeState');
        if (serializedState === null) {
            return undefined;
        }
        return JSON.parse(serializedState);
    } catch (err) {
        return undefined;
    }
}

// Load initial state from storage
const persistedState = loadState();

// Reinitialize store with persisted state
const rehydratedStore = configureStore({
    reducer: {
        user: userReducer,
        product: productReducer,
        cart: cartReducer
    },
    preloadedState: persistedState,
    middleware: (getDefaultMiddleware) =>
        getDefaultMiddleware().concat(thunk, apiMiddleware),
    devTools: process.env.NODE_ENV !== 'production'
});

// Subscribe to store updates to save new state
rehydratedStore.subscribe(() => {
    saveState(rehydratedStore.getState());
});

// Custom logger middleware with additional metadata
const enhancedLoggerMiddleware = (storeAPI) => (next) => (action) => {
    const { getState } = storeAPI;
    const prevState = getState();
    const result = next(action);
    const nextState = getState();
    console.groupCollapsed('Action:', action.type);
    console.log('Previous state:', prevState);
    console.log('Action payload:', action.payload);
    console.log('Next state:', nextState);
    console.groupEnd();
    return result;
};

// Applying enhanced logger middleware
const enhancedLoggerStore = configureStore({
    reducer: {
        user: userReducer,
        product: productReducer,
        cart: cartReducer
    },
    middleware: (getDefaultMiddleware) =>
        getDefaultMiddleware().concat(enhancedLoggerMiddleware, thunk, apiMiddleware),
    devTools: process.env.NODE_ENV !== 'production'
});