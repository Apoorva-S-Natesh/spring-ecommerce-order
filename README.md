# Spring E-commerce Orders

## JDBC to JPA Migration - Step 1

### Migration Setup
- [x] Update `build.gradle.kts` with JPA dependencies
- [x] Configure `application.properties` for H2/MySQL compatibility
- [x] Set up `data.sql` for test data

---

## Product
- [x] **Entity** - JPA annotations, validation, relationships
- [x] **Repository** - JpaRepository with custom queries
- [x] **Service** - CRUD operations, business logic
- [x] **Controller** - AdminController endpoints
- [x] **Tests** - Unit and integration tests

## ProductOption
- [x] **Entity** - JPA mapping, business methods (`subtract`, `toString`)
- [x] **Repository** - JpaRepository with name validation
- [x] **Service** - Unified add/update with `saveProductOption()`
- [x] **Controller** - REST endpoints in AdminController
- [x] **Tests** - Service layer testing

## Member
- [x] **Entity** - User management, roles, cart relationship
- [x] **Repository** - JpaRepository migration from JDBC
- [x] **Service** - Authentication, user operations
- [x] **Controller** - User management endpoints
- [x] **Tests** - Entity and service testing

## Cart
- [x] **Entity** - Shopping cart with member relationship
- [x] **Repository** - JpaRepository with cart operations
- [x] **Service** - Cart management logic
- [x] **Controller** - Cart operations
- [x] **Tests** - Integration testing

## CartItem
- [x] **Entity** - Cart-product relationship, `modify()` method
- [x] **Repository** - JpaRepository with `@Modifying` queries
- [x] **Service** - Refactored `saveCartItem()` (add/update unified)
- [x] **Controller** - CRUD operations
- [x] **Tests** - Unit tests with Mockito, integration tests with data.sql

---

## Key points

### Service Layer Refactoring
- [x] **Single Responsibility** - Broke down complex methods into focused private functions
- [x] **Unified Operations** - `saveProductOption()` and `saveCartItem()` handle both add/update
- [x] **Kotlin Style** - Nullable parameters with defaults, proper type inference

### Testing Strategy
- [x] **Unit Tests** - Mock-based testing for service logic
- [x] **Integration Tests** - Full Spring context with real database
- [x] **Test Data** - Leveraging `data.sql` for consistent test scenarios

### Next Steps
- [x] **Pagination** - Add `Pageable` support to controllers
- [x] **Inventory Management** - Stock tracking and validation
- [ ] **Performance** - Query optimization and caching

## Place Order and Payment
- [x] **Setup Stripe** - add secret-key to application-properties or application.yml(External API)
  - [x] key hidden from gitHub codeBase (environment variable management in springBoot)
  - [] check key is correctly loaded with StripeProperties (@ConfigurationProperties)
  - [x] @EnableConfigurationProperties(StripeProperties::class) on class Application
- [x] **Entity** - OrderItem (id, quantity, price, ProductOption @ManyToOne - one directional)
- [x] **DTO** - PlaceOrderRequest(productOptionId, quantity,paymentDetails)
        -[x] Validate (valid ProductOptionID, positive quantity, notBlank payment) 
  - [x] PaymentResponse 
- [x] **Repository**
- [x] **Service** - StripeClient - 
  - [x] call Stripe Payment Intents API(/v1/payment_intents)
  - [x] request (Parameters - amount, currency, payment_method, confirm=true)
  - [x] Order amount based on ProductOption Price(from Product) and quantity
  - [x] Handle API response to extract checkout session ID (payment Status)
  - [x] payment successful -> Decrease ProductOption quantity by ordered quantity 
  - [x] payment successful -> Remove CartItem from member's cart (CartItemService.deleteCartItemById)
  - [x] payment successful -> Update Cart Quantity (CartItem removed)
  - [] Set timeouts for Stripe API calls using RestClient to avoid holding database connections during slow responses.
  - [x] Handle transaction propagation (REQUIRED for main transaction)
- [x] **Controller** - placeOrder endpoints(POST /api/orders/  @RequestBody placeOrderRequest) 
  - verify endpoint requires authentication
- [x] **Tests** - Unit and integration tests
  - [x] Test API call with valid test cards from Stripe's documentation. (Verify Response fields -id, status)
  - [x] declined payments (insufficient funds, invalid card)
- [x] **Exception Handling**
  - [x] Exceptions from StripeClient - map to specific reasons (expired session, Invalid payment Method)
  - [x] Return user friendly error messages - ResponseEntity.badRequest().body(ErrorResponse(message))
- [x] Validate stock availability (fetch user's cart, CartItem, ProductOption) - decide where should this validation happen
- [x] @Transactional (Stock update and CartItem removal)
- [x] Server side polling to handle payment - Synchronous status check

## Orders - Store and display order details
- [x] **Entity** 
  - [x] Order (OrderItem @OneToMany, memberId, Payment @OneToOne)
  - [x] Payment (id, checkout_session_id, amount, currency, status, payment_method, created, and last_payment_error, linked to Order.)
- [x] **DTO** 
  -   [x] - OrderResponse (id, LocalDateTme, status, purchasedItem, checkoutSessionId, amount)
  -   [x] - OrderItemResponse (quantity, price, productOptionId)
- [x] **Service** - Create an order, process payment, update stock, remove cart items
- [x] **Controller** 
- GET /api/orders - return a list of orders for authenticated user, 
- GET /api/orders/{orderId} - Retrieve details of a specific order for the authenticated user.
- GET /api/admin/orders - Admin can view all orders
 - [x] **Tests** - Integration tests (no orders, invalid user, failed payment)

## Deployment
- [x] Deployment Script (shell script) - automate deployment
  - [X] steps to copy the build, start application with nohup
- [X] Configure environment variable - secure stripe API key
- [X] Configure CORS to allow client requests(CorsConfiguration)
- [X] Set allowedOrigins to * allow methods (GET, POST, etc.), set maxAge to 1800 seconds.
- [X] Test CORS with AcceptanceTest to verify headers
- [X] Deploy to AWS EC2

## Next Steps - Webhook
- [] setup Webhook endpoint - handle asynchronous payment flows (WebHook controller POST /api/webhooks/stripe)
- [] Configure Stripe dashboard - add new webhook endpoint with the URL
- [] Verify webhook Signature (Stripe signature - verify authenticity)
- [] Process webhook events - parse JSON payload - exteact event type and data
- [] test (payment_intent.succeeded and payment_intent.failed)
