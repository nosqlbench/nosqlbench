# JDBC E-commerce Workload

## Overview

The `jdbc_ecommerce.yaml` workload simulates a realistic e-commerce platform with complex relationships and varied query patterns. This workload is ideal for testing multi-phase operations, realistic data distributions, and mixed transactional/analytical workloads.

## Scenario Description

This workload simulates a complete e-commerce platform with:
- **Customer Management**: Customer profiles with tier-based segmentation
- **Product Catalog**: Products with categories, brands, and pricing
- **Order Processing**: Order lifecycle with line items and status tracking
- **Review System**: Product reviews and ratings
- **Address Management**: Customer shipping and billing addresses

## Schema Design

### Tables Created

1. **customers** (9 columns) - Customer profiles
   - `customer_id`, `email`, `first_name`, `last_name`, `phone`, `birth_date`, `registration_date`, `status`, `tier`

2. **addresses** (8 columns) - Customer addresses
   - `address_id`, `customer_id`, `address_type`, `street_address`, `city`, `state`, `zip_code`, `country`

3. **categories** (3 columns) - Product categories
   - `category_id`, `category_name`, `description`

4. **products** (8 columns) - Product catalog
   - `product_id`, `product_name`, `description`, `category_id`, `brand`, `price`, `weight_kg`, `status`

5. **orders** (7 columns) - Order transactions
   - `order_id`, `customer_id`, `order_date`, `status`, `payment_method`, `shipping_method`, `total_amount`

6. **order_items** (5 columns) - Order line items
   - `order_item_id`, `order_id`, `product_id`, `quantity`, `unit_price`

7. **reviews** (9 columns) - Product reviews
   - `review_id`, `customer_id`, `product_id`, `rating`, `title`, `review_text`, `review_date`, `verified_purchase`, `helpful_votes`

## Operations and Phases

### Rampup Phase
**Purpose**: Load realistic e-commerce data with proper relationships

**Operations**:
- `insert_customers` (ratio: 10) - Customer profiles
- `insert_addresses` (ratio: 15) - Customer addresses (1.5 per customer)
- `insert_categories` (ratio: 1) - Product categories
- `insert_products` (ratio: 20) - Product catalog
- `insert_orders` (ratio: 100) - Customer orders
- `insert_order_items` (ratio: 300) - Order line items (3 per order)
- `insert_reviews` (ratio: 50) - Product reviews

**Stride Calculation**: 10 + 15 + 1 + 20 + 100 + 300 + 50 = **496 operations per stride**

**Data Relationships**:
- 10 customers → 15 addresses → 100 orders → 300 order items
- 20 products → 50 reviews
- Realistic e-commerce ratios: multiple addresses per customer, multiple items per order

### Transactions Phase
**Purpose**: Simulate typical e-commerce user interactions

**Operations**:
- `query_customer_lookup` (ratio: 20) - Customer profile retrieval
- `query_product_search` (ratio: 25) - Product catalog browsing
- `query_product_details` (ratio: 15) - Individual product details
- `query_order_history` (ratio: 10) - Customer order history
- `query_popular_products` (ratio: 8) - Popular product listings

**Stride Calculation**: 20 + 25 + 15 + 10 + 8 = **78 operations per stride**

### Analytics Phase
**Purpose**: Business intelligence and reporting queries

**Operations**:
- `query_sales_analytics` (ratio: 15) - Sales performance metrics
- `query_customer_analytics` (ratio: 10) - Customer segmentation analysis
- `query_product_performance` (ratio: 8) - Product performance analytics

**Stride Calculation**: 15 + 10 + 8 = **33 operations per stride**

## Binding Patterns

### Realistic E-commerce Data
```yaml
# Customer data with realistic distributions
customer_id: Uniform(1,50000)->long
customer_email: Template('customer{}@example.com', Identity())
customer_first_name: FirstNames()
customer_last_name: LastNames()
customer_tier: WeightedStrings('bronze:50;silver:30;gold:15;platinum:5')
customer_status: WeightedStrings('active:80;inactive:15;suspended:4;deleted:1')

# Product data with business patterns
product_id: Uniform(1,10000)->long
category_name: WeightedStrings('Electronics:15;Clothing:12;Home & Garden:10;Sports:8;Books:7;Toys:6;Health:5;Beauty:5;Automotive:4;Jewelry:3;Food:3;Pet Supplies:3;Office:2;Baby:2;Industrial:2;Other:13')
brand: WeightedStrings('BrandA:20;BrandB:15;BrandC:12;BrandD:10;BrandE:8;BrandF:6;BrandG:5;BrandH:4;BrandI:3;BrandJ:3;Generic:14')
price: Normal(50.0,30.0); Max(1.0) -> double
product_status: WeightedStrings('active:85;discontinued:10;out_of_stock:4;pending:1')

# Order data with seasonal patterns
order_status: WeightedStrings('pending:20;processing:15;shipped:25;delivered:30;cancelled:8;returned:2')
payment_method: WeightedStrings('credit_card:60;debit_card:20;paypal:10;apple_pay:5;google_pay:3;bank_transfer:2')
order_total: Normal(100.0,50.0); Max(5.0) -> double

# Review data with rating distribution
rating: Uniform(1,5)->int
verified_purchase: WeightedStrings('true:80;false:20')
```

## Usage Examples

### Complete E-commerce Simulation
```bash
# Run full e-commerce workload
nb5 jdbc_ecommerce.yaml default dburl=jdbc:presto://host:8080/memory?user=testuser use_hikaricp=true threads=auto

# Individual phases
nb5 jdbc_ecommerce.yaml default.drop dburl=jdbc:presto://host:8080/memory?user=testuser use_hikaricp=true
nb5 jdbc_ecommerce.yaml default.schema dburl=jdbc:presto://host:8080/memory?user=testuser use_hikaricp=true
nb5 jdbc_ecommerce.yaml default.rampup dburl=jdbc:presto://host:8080/memory?user=testuser use_hikaricp=true
nb5 jdbc_ecommerce.yaml default.transactions dburl=jdbc:presto://host:8080/memory?user=testuser use_hikaricp=true
nb5 jdbc_ecommerce.yaml default.analytics dburl=jdbc:presto://host:8080/memory?user=testuser use_hikaricp=true
```

### High-Scale E-commerce Testing
```bash
# Large-scale e-commerce simulation
nb5 jdbc_ecommerce.yaml default dburl=jdbc:presto://host:8080/memory?user=testuser use_hikaricp=true \
  threads=8 connections=8 rampup_cycles=25000 transaction_cycles=10000

# Peak traffic simulation
nb5 jdbc_ecommerce.yaml default.transactions dburl=jdbc:presto://host:8080/memory?user=testuser use_hikaricp=true \
  threads=16 connections=16 transaction_cycles=50000

# Business intelligence workload
nb5 jdbc_ecommerce.yaml default.analytics dburl=jdbc:presto://host:8080/memory?user=testuser use_hikaricp=true \
  threads=4 connections=8 transaction_cycles=5000
```

### Stride Optimization
```bash
# Optimal cycles (multiples of stride)
# Rampup stride = 496, so use: 496, 992, 2480, 4960, 24800, etc.
nb5 jdbc_ecommerce.yaml default.rampup rampup_cycles=24800

# Transactions stride = 78, so use: 78, 156, 780, 1560, 7800, etc.
nb5 jdbc_ecommerce.yaml default.transactions transaction_cycles=7800

# Analytics stride = 33, so use: 33, 66, 330, 660, 3300, etc.
nb5 jdbc_ecommerce.yaml default.analytics transaction_cycles=3300
```

## Customization Parameters

### Template Variables
- `schemaname` (default: ecommerce) - Database schema name
- `connections` (default: 5) - Connection pool size
- `rampup_cycles` (default: 25000) - Number of data loading operations
- `transaction_cycles` (default: 10000) - Number of transactional operations

### Custom Configuration Example
```bash
nb5 jdbc_ecommerce.yaml default \
  dburl=jdbc:presto://host:8080/memory?user=testuser \
  use_hikaricp=true \
  schemaname=ecommerce_prod \
  connections=12 \
  rampup_cycles=49600 \
  transaction_cycles=15600 \
  threads=8
```

## Performance Characteristics

### Expected Throughput
- **Rampup Phase**: 1000-3000 ops/sec (complex inserts with relationships)
- **Transactions Phase**: 2000-8000 ops/sec (mixed read operations)
- **Analytics Phase**: 100-1000 ops/sec (aggregation queries)

### Resource Usage
- **Memory**: Moderate to High (complex queries, multiple tables)
- **CPU**: Moderate (joins, aggregations, string operations)
- **I/O**: Moderate to High (multi-table operations)

### Scaling Behavior
- **Single Thread**: Baseline performance measurement
- **2-4 Threads**: Good scaling for transactional workload
- **8+ Threads**: May hit database limits on complex analytics

## Query Analysis

### Transaction Queries
1. **Customer Lookup**: Point queries by customer ID
2. **Product Search**: Filtered product listings
3. **Product Details**: Individual product information
4. **Order History**: Customer-specific order retrieval
5. **Popular Products**: Simple product rankings

### Analytics Queries
1. **Sales Analytics**: Order count and revenue metrics
2. **Customer Analytics**: Customer tier distribution
3. **Product Performance**: Product status analysis

### Query Complexity
- **Low Complexity**: Single-table lookups and counts
- **Medium Complexity**: Filtered queries with ORDER BY
- **Higher Complexity**: Multi-table joins (disabled for PrestoDB compatibility)

## Real-World Simulation

### Business Scenarios
- **Customer Registration**: New customer onboarding
- **Product Browsing**: Catalog search and filtering
- **Order Placement**: Shopping cart to order conversion
- **Order Fulfillment**: Status tracking and updates
- **Customer Service**: Order history and account management
- **Business Intelligence**: Sales reporting and analytics

### Data Patterns
- **Customer Tiers**: Bronze (50%), Silver (30%), Gold (15%), Platinum (5%)
- **Order Status**: Delivered (30%), Shipped (25%), Pending (20%), Processing (15%)
- **Product Categories**: Electronics (15%), Clothing (12%), Home & Garden (10%)
- **Payment Methods**: Credit Card (60%), Debit Card (20%), PayPal (10%)

## Troubleshooting

### Common Issues
1. **Stride Warnings**: Use cycles that are multiples of 496 (rampup), 78 (transactions), or 33 (analytics)
2. **Data Relationships**: Ensure rampup completes before running transactions
3. **Memory Usage**: Complex queries may require database memory tuning
4. **Connection Limits**: High concurrency may hit database connection limits

### Performance Optimization
```bash
# Test individual phases
nb5 jdbc_ecommerce.yaml default.rampup rampup_cycles=496
nb5 jdbc_ecommerce.yaml default.transactions transaction_cycles=78
nb5 jdbc_ecommerce.yaml default.analytics transaction_cycles=33

# Monitor query performance
nb5 jdbc_ecommerce.yaml default.transactions --log-level DEBUG transaction_cycles=78

# Validate data relationships
nb5 jdbc_ecommerce.yaml default.rampup rampup_cycles=496 --log-level INFO
```

## Best Practices

1. **Sequential Execution**: Run phases in order (drop → schema → rampup → transactions → analytics)
2. **Data Validation**: Verify row counts after rampup phase
3. **Resource Monitoring**: Watch database CPU, memory, and connection usage
4. **Gradual Scaling**: Start with low thread counts and scale incrementally
5. **Realistic Testing**: Use business-appropriate cycle counts for realistic simulation
6. **Phase Isolation**: Test individual phases to isolate performance characteristics
7. **Stride Alignment**: Use optimal cycle counts for complete stride utilization
