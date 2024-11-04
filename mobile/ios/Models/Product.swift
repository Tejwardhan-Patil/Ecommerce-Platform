import Foundation

class ProductService {
    static let shared = ProductService()

    private init() {}

    func fetchProducts(completion: @escaping ([Product]?, Error?) -> Void) {
        let url = URL(string: "https://website.com/api/products")!
        let task = URLSession.shared.dataTask(with: url) { data, response, error in
            guard let data = data, error == nil else {
                completion(nil, error)
                return
            }

            do {
                let products = try JSONDecoder().decode([Product].self, from: data)
                completion(products, nil)
            } catch let decodeError {
                completion(nil, decodeError)
            }
        }
        task.resume()
    }
    
    func searchProducts(query: String, completion: @escaping ([Product]?, Error?) -> Void) {
        var urlComponents = URLComponents(string: "https://website.com/api/products/search")!
        urlComponents.queryItems = [URLQueryItem(name: "q", value: query)]
        
        let task = URLSession.shared.dataTask(with: urlComponents.url!) { data, response, error in
            guard let data = data, error == nil else {
                completion(nil, error)
                return
            }

            do {
                let products = try JSONDecoder().decode([Product].self, from: data)
                completion(products, nil)
            } catch let decodeError {
                completion(nil, decodeError)
            }
        }
        task.resume()
    }

    func filterProducts(byCategory categoryId: String, completion: @escaping ([Product]?, Error?) -> Void) {
        var urlComponents = URLComponents(string: "https://website.com/api/products")!
        urlComponents.queryItems = [URLQueryItem(name: "categoryId", value: categoryId)]
        
        let task = URLSession.shared.dataTask(with: urlComponents.url!) { data, response, error in
            guard let data = data, error == nil else {
                completion(nil, error)
                return
            }

            do {
                let products = try JSONDecoder().decode([Product].self, from: data)
                completion(products, nil)
            } catch let decodeError {
                completion(nil, decodeError)
            }
        }
        task.resume()
    }

    func sortProducts(by option: SortOption, completion: @escaping ([Product]?, Error?) -> Void) {
        var urlComponents = URLComponents(string: "https://website.com/api/products")!
        urlComponents.queryItems = [URLQueryItem(name: "sort", value: option.rawValue)]
        
        let task = URLSession.shared.dataTask(with: urlComponents.url!) { data, response, error in
            guard let data = data, error == nil else {
                completion(nil, error)
                return
            }

            do {
                let products = try JSONDecoder().decode([Product].self, from: data)
                completion(products, nil)
            } catch let decodeError {
                completion(nil, decodeError)
            }
        }
        task.resume()
    }
}