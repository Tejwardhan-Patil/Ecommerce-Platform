import UIKit

class ViewController: UIViewController {

    // MARK: - UI Elements
    var tableView: UITableView!
    var cartButton: UIBarButtonItem!
    var products: [Product] = []
    var cart: [Product] = []

    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Setting up UI
        setupTableView()
        setupCartButton()
        
        // Load products
        loadProducts()
    }
    
    // MARK: - Setup UI
    func setupTableView() {
        tableView = UITableView()
        tableView.delegate = self
        tableView.dataSource = self
        tableView.register(ProductTableViewCell.self, forCellReuseIdentifier: "ProductCell")
        view.addSubview(tableView)
        
        // Constraints for TableView
        tableView.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
            tableView.topAnchor.constraint(equalTo: view.topAnchor),
            tableView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            tableView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            tableView.bottomAnchor.constraint(equalTo: view.bottomAnchor)
        ])
    }

    func setupCartButton() {
        cartButton = UIBarButtonItem(title: "Cart", style: .plain, target: self, action: #selector(goToCart))
        navigationItem.rightBarButtonItem = cartButton
    }

    // MARK: - Load Products
    func loadProducts() {
        ProductService.shared.fetchProducts { [weak self] products in
            guard let self = self else { return }
            self.products = products
            self.tableView.reloadData()
        }
    }

    // MARK: - Cart Handling
    @objc func goToCart() {
        let cartViewController = CartViewController()
        cartViewController.cart = cart
        navigationController?.pushViewController(cartViewController, animated: true)
    }
    
    func addToCart(product: Product) {
        cart.append(product)
        cartButton.title = "Cart (\(cart.count))"
    }
}

// MARK: - UITableViewDataSource
extension ViewController: UITableViewDataSource {
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return products.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "ProductCell", for: indexPath) as! ProductTableViewCell
        let product = products[indexPath.row]
        cell.configure(with: product)
        cell.addToCartAction = { [weak self] in
            self?.addToCart(product: product)
        }
        return cell
    }
}

// MARK: - UITableViewDelegate
extension ViewController: UITableViewDelegate {
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let product = products[indexPath.row]
        let productDetailVC = ProductDetailViewController()
        productDetailVC.product = product
        navigationController?.pushViewController(productDetailVC, animated: true)
    }
}

// MARK: - ProductTableViewCell
class ProductTableViewCell: UITableViewCell {
    
    var productImageView: UIImageView!
    var nameLabel: UILabel!
    var priceLabel: UILabel!
    var addToCartButton: UIButton!
    
    var addToCartAction: (() -> Void)?
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func setupUI() {
        productImageView = UIImageView()
        nameLabel = UILabel()
        priceLabel = UILabel()
        addToCartButton = UIButton(type: .system)
        addToCartButton.setTitle("Add to Cart", for: .normal)
        addToCartButton.addTarget(self, action: #selector(addToCartTapped), for: .touchUpInside)
        
        contentView.addSubview(productImageView)
        contentView.addSubview(nameLabel)
        contentView.addSubview(priceLabel)
        contentView.addSubview(addToCartButton)
        
        // Layout Constraints
        productImageView.translatesAutoresizingMaskIntoConstraints = false
        nameLabel.translatesAutoresizingMaskIntoConstraints = false
        priceLabel.translatesAutoresizingMaskIntoConstraints = false
        addToCartButton.translatesAutoresizingMaskIntoConstraints = false
        
        NSLayoutConstraint.activate([
            productImageView.leadingAnchor.constraint(equalTo: contentView.leadingAnchor, constant: 10),
            productImageView.centerYAnchor.constraint(equalTo: contentView.centerYAnchor),
            productImageView.widthAnchor.constraint(equalToConstant: 60),
            productImageView.heightAnchor.constraint(equalToConstant: 60),
            
            nameLabel.leadingAnchor.constraint(equalTo: productImageView.trailingAnchor, constant: 10),
            nameLabel.topAnchor.constraint(equalTo: contentView.topAnchor, constant: 10),
            
            priceLabel.leadingAnchor.constraint(equalTo: productImageView.trailingAnchor, constant: 10),
            priceLabel.topAnchor.constraint(equalTo: nameLabel.bottomAnchor, constant: 10),
            
            addToCartButton.trailingAnchor.constraint(equalTo: contentView.trailingAnchor, constant: -10),
            addToCartButton.centerYAnchor.constraint(equalTo: contentView.centerYAnchor)
        ])
    }
    
    @objc func addToCartTapped() {
        addToCartAction?()
    }
    
    func configure(with product: Product) {
        productImageView.image = UIImage(named: product.imageName)
        nameLabel.text = product.name
        priceLabel.text = "$\(product.price)"
    }
}

// MARK: - Product Model
struct Product {
    var name: String
    var price: Double
    var imageName: String
}

// MARK: - ProductService
class ProductService {
    
    static let shared = ProductService()
    
    private init() {}
    
    func fetchProducts(completion: @escaping ([Product]) -> Void) {
        // Mock data
        let products = [
            Product(name: "Shoes", price: 79.99, imageName: "shoes"),
            Product(name: "Jacket", price: 149.99, imageName: "jacket"),
            Product(name: "Hat", price: 19.99, imageName: "hat")
        ]
        completion(products)
    }
}

// MARK: - CartViewController
class CartViewController: UIViewController {

    var cart: [Product] = []
    var tableView: UITableView!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        title = "Cart"
        setupTableView()
    }
    
    func setupTableView() {
        tableView = UITableView()
        tableView.delegate = self
        tableView.dataSource = self
        tableView.register(UITableViewCell.self, forCellReuseIdentifier: "CartCell")
        view.addSubview(tableView)
        
        // Constraints for TableView
        tableView.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
            tableView.topAnchor.constraint(equalTo: view.topAnchor),
            tableView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            tableView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            tableView.bottomAnchor.constraint(equalTo: view.bottomAnchor)
        ])
    }
}

// MARK: - UITableViewDataSource for Cart
extension CartViewController: UITableViewDataSource {
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return cart.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "CartCell", for: indexPath)
        let product = cart[indexPath.row]
        cell.textLabel?.text = "\(product.name) - $\(product.price)"
        return cell
    }
}

// MARK: - UITableViewDelegate for Cart
extension CartViewController: UITableViewDelegate {
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
    }
}