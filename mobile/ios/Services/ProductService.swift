import Foundation
import UIKit

struct Product: Codable {
    let id: Int
    let name: String
    let description: String
    let price: Double
    let imageUrl: String?
}

enum ProductError: Error {
    case invalidResponse
    case networkError(Error)
    case dataError
}

protocol ProductServiceProtocol {
    func fetchProducts(completion: @escaping (Result<[Product], ProductError>) -> Void)
    func fetchProduct(byID id: Int, completion: @escaping (Result<Product, ProductError>) -> Void)
    func createProduct(_ product: Product, completion: @escaping (Result<Product, ProductError>) -> Void)
    func updateProduct(_ product: Product, completion: @escaping (Result<Product, ProductError>) -> Void)
    func deleteProduct(byID id: Int, completion: @escaping (Result<Bool, ProductError>) -> Void)
}

class ProductService: ProductServiceProtocol {
    
    private let apiUrl = "https://api.website.com/products"
    private let cache = NSCache<NSString, Product>()
    private let session: URLSession
    
    init(session: URLSession = .shared) {
        self.session = session
    }

    func fetchProducts(completion: @escaping (Result<[Product], ProductError>) -> Void) {
        guard let url = URL(string: apiUrl) else {
            completion(.failure(.dataError))
            return
        }
        
        if let cachedProducts = getCachedProducts() {
            completion(.success(cachedProducts))
            return
        }
        
        let task = session.dataTask(with: url) { data, response, error in
            if let error = error {
                completion(.failure(.networkError(error)))
                return
            }
            
            guard let data = data, let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 else {
                completion(.failure(.invalidResponse))
                return
            }
            
            do {
                let products = try JSONDecoder().decode([Product].self, from: data)
                self.cacheProducts(products)
                completion(.success(products))
            } catch {
                completion(.failure(.dataError))
            }
        }
        task.resume()
    }

    func fetchProduct(byID id: Int, completion: @escaping (Result<Product, ProductError>) -> Void) {
        if let cachedProduct = cache.object(forKey: NSString(string: "\(id)")) {
            completion(.success(cachedProduct))
            return
        }
        
        guard let url = URL(string: "\(apiUrl)/\(id)") else {
            completion(.failure(.dataError))
            return
        }
        
        let task = session.dataTask(with: url) { data, response, error in
            if let error = error {
                completion(.failure(.networkError(error)))
                return
            }
            
            guard let data = data, let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 else {
                completion(.failure(.invalidResponse))
                return
            }
            
            do {
                let product = try JSONDecoder().decode(Product.self, from: data)
                self.cache.setObject(product, forKey: NSString(string: "\(id)"))
                completion(.success(product))
            } catch {
                completion(.failure(.dataError))
            }
        }
        task.resume()
    }
    
    func createProduct(_ product: Product, completion: @escaping (Result<Product, ProductError>) -> Void) {
        guard let url = URL(string: apiUrl) else {
            completion(.failure(.dataError))
            return
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        
        do {
            let data = try JSONEncoder().encode(product)
            request.httpBody = data
        } catch {
            completion(.failure(.dataError))
            return
        }
        
        let task = session.dataTask(with: request) { data, response, error in
            if let error = error {
                completion(.failure(.networkError(error)))
                return
            }
            
            guard let data = data, let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 201 else {
                completion(.failure(.invalidResponse))
                return
            }
            
            do {
                let createdProduct = try JSONDecoder().decode(Product.self, from: data)
                self.cache.setObject(createdProduct, forKey: NSString(string: "\(createdProduct.id)"))
                completion(.success(createdProduct))
            } catch {
                completion(.failure(.dataError))
            }
        }
        task.resume()
    }

    func updateProduct(_ product: Product, completion: @escaping (Result<Product, ProductError>) -> Void) {
        guard let url = URL(string: "\(apiUrl)/\(product.id)") else {
            completion(.failure(.dataError))
            return
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "PUT"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        
        do {
            let data = try JSONEncoder().encode(product)
            request.httpBody = data
        } catch {
            completion(.failure(.dataError))
            return
        }
        
        let task = session.dataTask(with: request) { data, response, error in
            if let error = error {
                completion(.failure(.networkError(error)))
                return
            }
            
            guard let data = data, let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 else {
                completion(.failure(.invalidResponse))
                return
            }
            
            do {
                let updatedProduct = try JSONDecoder().decode(Product.self, from: data)
                self.cache.setObject(updatedProduct, forKey: NSString(string: "\(updatedProduct.id)"))
                completion(.success(updatedProduct))
            } catch {
                completion(.failure(.dataError))
            }
        }
        task.resume()
    }
    
    func deleteProduct(byID id: Int, completion: @escaping (Result<Bool, ProductError>) -> Void) {
        guard let url = URL(string: "\(apiUrl)/\(id)") else {
            completion(.failure(.dataError))
            return
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "DELETE"
        
        let task = session.dataTask(with: request) { _, response, error in
            if let error = error {
                completion(.failure(.networkError(error)))
                return
            }
            
            guard let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 204 else {
                completion(.failure(.invalidResponse))
                return
            }
            
            self.cache.removeObject(forKey: NSString(string: "\(id)"))
            completion(.success(true))
        }
        task.resume()
    }

    private func cacheProducts(_ products: [Product]) {
        for product in products {
            cache.setObject(product, forKey: NSString(string: "\(product.id)"))
        }
    }

    private func getCachedProducts() -> [Product]? {
        let cachedKeys = cache.allObjects
        guard !cachedKeys.isEmpty else { return nil }
        return cachedKeys.compactMap { $0 as? Product }
    }
}