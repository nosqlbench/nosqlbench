# JDBC Simple Workload

## Overview

The `jdbc_simple.yaml` workload demonstrates fundamental database operations with a basic employee management system. This workload is ideal for testing basic CRUD operations, simple joins, and aggregation queries.

## Scenario Description

This workload simulates a simple business system with three main entities:
- **Employees**: Company staff with departments and salary information
- **Products**: Product catalog with categories and pricing
- **Orders**: Customer orders linking to products

## Schema Design

### Tables Created

1. **employees** (8 columns)
   - `id` (varchar): Employee identifier
   - `name` (varchar): Employee full name
   - `email` (varchar): Email address
   - `age` (varchar): Employee age
   - `salary` (varchar): Annual salary
   - `department` (varchar): Department assignment
   - `hire_date` (varchar): Date of hire
   - `active` (varchar): Employment status

2. **products** (5 columns)
   - `product_id` (varchar): Product identifier
   - `product_name` (varchar): Product name
   - `category` (varchar): Product category
   - `price` (varchar): Product price
   - `in_stock` (varchar): Availability status

3. **orders** (6 columns)
   - `order_id` (varchar): Order identifier
   - `customer_id` (varchar): Customer identifier
   - `product_id` (varchar): Ordered product
   - `quantity` (varchar): Order quantity
   - `order_date` (varchar): Order date
   - `status` (varchar): Order status

## Operations and Phases

### Rampup Phase
**Purpose**: Load initial data into tables

**Operations**:
- `insert_employees` (ratio: 10) - Insert employee records
- `insert_products` (ratio: 20) - Insert product catalog
- `insert_orders` (ratio: 100) - Insert order transactions

**Stride Calculation**: 10 + 20 + 100 = **130 operations per stride**

**Data Distribution**:
- For every 130 operations: 10 employees, 20 products, 100 orders
- Creates realistic 1:2:10 ratio reflecting business patterns

### Main Phase
**Purpose**: Execute read-heavy workload with analytical queries

**Operations**:
- `query_employee_count` (ratio: 20) - Count total employees
- `query_employees_by_department` (ratio: 15) - Group employees by department
- `query_product_count` (ratio: 15) - Count total products
- `query_products_by_category` (ratio: 15) - Group products by category
- `query_order_count` (ratio: 15) - Count total orders
- `query_order_summary` (ratio: 10) - Group orders by status
- `query_sales_by_category` (ratio: 10) - Join orders with products for sales analysis

**Stride Calculation**: 20 + 15 + 15 + 15 + 15 + 10 + 10 = **100 operations per stride**

## Binding Patterns

### Data Generation
```yaml
# Employee data
id: Uniform(1,100000)->long                    # Unique employee IDs
name: Template('Name-{}', Identity())          # Generated names
email: Template('user{}@example.com', Identity()) # Email addresses
age: Uniform(18,80)->int                       # Realistic age range
salary: Normal(50000,15000) -> double          # Normal salary distribution
department: WeightedStrings('Engineering:30;Sales:25;Marketing:20;HR:15;Finance:10')

# Product data  
product_id: Uniform(1,10000)->long             # Product catalog size
category: WeightedStrings('Electronics:25;Clothing:20;Books:15;Home:15;Sports:10;Other:15')
price: Normal(100,50); Max(1.0) -> double      # Price with minimum $1

# Order data
order_id: Uniform(1,500000)->long              # Large order volume
status: WeightedStrings('pending:20;shipped:30;delivered:40;cancelled:10')
```

### Advanced Binding Example
```yaml
# Calculated pricing with Save/Load pattern
base_price: Normal(100,50); Save('base_price') -> double
discount_rate: Uniform(0,20); Div(100); Save('discount_rate') -> double  
discounted_price: Load('base_price'); Load('discount_rate'); Expr('base_price * (1 - discount_rate)') -> double
```

## Usage Examples

### Basic Execution
```bash
# Run complete workload
nb5 jdbc_simple.yaml default dburl=jdbc:presto://host:8080/memory?user=testuser use_hikaricp=true

# Individual phases
nb5 jdbc_simple.yaml default.drop dburl=jdbc:presto://host:8080/memory?user=testuser use_hikaricp=true
nb5 jdbc_simple.yaml default.schema dburl=jdbc:presto://host:8080/memory?user=testuser use_hikaricp=true
nb5 jdbc_simple.yaml default.rampup dburl=jdbc:presto://host:8080/memory?user=testuser use_hikaricp=true
nb5 jdbc_simple.yaml default.main dburl=jdbc:presto://host:8080/memory?user=testuser use_hikaricp=true
```

### Performance Testing
```bash
# Single-threaded (most reliable)
nb5 jdbc_simple.yaml default dburl=jdbc:presto://host:8080/memory?user=testuser use_hikaricp=true threads=1

# Multi-threaded with custom parameters
nb5 jdbc_simple.yaml default dburl=jdbc:presto://host:8080/memory?user=testuser use_hikaricp=true \
  threads=4 connections=4 rampup_cycles=10000 main_cycles=2000

# High-throughput testing
nb5 jdbc_simple.yaml default dburl=jdbc:presto://host:8080/memory?user=testuser use_hikaricp=true \
  threads=auto connections=8 rampup_cycles=50000 main_cycles=10000
```

### Stride Optimization
```bash
# Optimal cycles (multiples of stride)
# Rampup stride = 130, so use: 1300, 2600, 5200, 10400, etc.
nb5 jdbc_simple.yaml default.rampup rampup_cycles=10400

# Main stride = 100, so use: 1000, 2000, 5000, 10000, etc.  
nb5 jdbc_simple.yaml default.main main_cycles=5000
```

## Customization Parameters

### Template Variables
- `schemaname` (default: simple_fresh) - Database schema name
- `connections` (default: 5) - Connection pool size
- `rampup_cycles` (default: 5000) - Number of insert operations
- `main_cycles` (default: 1000) - Number of query operations

### Custom Configuration Example
```bash
nb5 jdbc_simple.yaml default \
  dburl=jdbc:presto://host:8080/memory?user=testuser \
  use_hikaricp=true \
  schemaname=my_test_schema \
  connections=10 \
  rampup_cycles=13000 \
  main_cycles=5000 \
  threads=6
```

## Performance Characteristics

### Expected Throughput
- **Rampup Phase**: 1000-5000 ops/sec (depending on database and hardware)
- **Main Phase**: 2000-10000 ops/sec (read-heavy workload)

### Resource Usage
- **Memory**: Low (simple aggregations)
- **CPU**: Moderate (basic joins and grouping)
- **I/O**: Moderate (balanced read/write)

### Scaling Behavior
- **Single Thread**: Baseline performance measurement
- **2-4 Threads**: Linear scaling for most databases
- **8+ Threads**: May hit database connection limits

## Query Analysis

### Query Types Executed
1. **Simple Aggregations**: `COUNT(*)` operations
2. **Group By Queries**: Department and category analysis
3. **Join Queries**: Orders with products for sales analysis
4. **Guaranteed Results**: All queries use aggregations that always return data

### Query Complexity
- **Low Complexity**: Single table counts
- **Medium Complexity**: GROUP BY with ORDER BY
- **Higher Complexity**: Two-table joins with aggregation

## Troubleshooting

### Common Issues
1. **Stride Warnings**: Use cycles that are multiples of 130 (rampup) or 100 (main)
2. **Connection Timeouts**: Reduce thread count or increase connection pool
3. **Empty Results**: Ensure rampup phase completes before running main phase

### Debug Commands
```bash
# Check workload syntax
nb5 jdbc_simple.yaml --dry-run

# Verbose logging
nb5 jdbc_simple.yaml default --log-level DEBUG

# Show template variables
nb5 help jdbc_simple.yaml
```

## Best Practices

1. **Start Simple**: Begin with single-threaded execution
2. **Monitor Resources**: Watch database CPU and memory usage
3. **Optimize Cycles**: Use multiples of stride for complete utilization
4. **Scale Gradually**: Increase threads incrementally
5. **Validate Data**: Check row counts after rampup phase
