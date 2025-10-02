# Subscription Plan Management API

## Admin Create Subscription Plan

**POST** `/api/payment/subscription-plans/admin/create`

**Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
    "stripePriceId": "price_1234567890abcdef",
    "name": "Premium Monthly",
    "description": "Access to all premium features with monthly billing",
    "amount": 29.99,
    "currency": "usd",
    "interval": "month",
    "intervalCount": 1
}
```

**Response:**
```json
{
    "success": true,
    "message": "Subscription plan created successfully",
    "data": {
        "id": "550e8400-e29b-41d4-a716-446655440000",
        "stripePriceId": "price_1234567890abcdef",
        "name": "Premium Monthly",
        "description": "Access to all premium features with monthly billing",
        "amount": 29.99,
        "currency": "usd",
        "interval": "month",
        "intervalCount": 1,
        "isActive": true,
        "createdAt": "2024-01-20T10:30:00",
        "updatedAt": "2024-01-20T10:30:00"
    }
}
```

## Get Active Subscription Plans (Frontend)

**GET** `/api/payment/subscription-plans/active`

**Headers:**
```
Content-Type: application/json
```

**Response:**
```json
{
    "success": true,
    "message": "Active subscription plans retrieved successfully",
    "data": [
        {
            "id": "550e8400-e29b-41d4-a716-446655440000",
            "stripePriceId": "price_1234567890abcdef",
            "name": "Premium Monthly",
            "description": "Access to all premium features with monthly billing",
            "amount": 29.99,
            "currency": "usd",
            "interval": "month",
            "intervalCount": 1,
            "isActive": true,
            "createdAt": "2024-01-20T10:30:00",
            "updatedAt": "2024-01-20T10:30:00"
        },
        {
            "id": "550e8400-e29b-41d4-a716-446655440001",
            "stripePriceId": "price_0987654321fedcba",
            "name": "Premium Yearly",
            "description": "Access to all premium features with yearly billing (2 months free)",
            "amount": 299.99,
            "currency": "usd",
            "interval": "year",
            "intervalCount": 1,
            "isActive": true,
            "createdAt": "2024-01-20T10:35:00",
            "updatedAt": "2024-01-20T10:35:00"
        }
    ]
}
```

## Postman Collection Example

### Create Subscription Plan Request
1. **Method:** POST
2. **URL:** `http://localhost:8083/api/payment/subscription-plans/admin/create`
3. **Headers:**
   - `Content-Type: application/json`
4. **Body (raw JSON):**
```json
{
    "stripePriceId": "price_1ORmsiAhPCODcPbGqXJWEFG1",
    "name": "Basic Monthly Plan",
    "description": "Basic access to all courses with monthly billing",
    "amount": 19.99,
    "currency": "usd",
    "interval": "month", 
    "intervalCount": 1
}
```

### Get Active Plans Request
1. **Method:** GET
2. **URL:** `http://localhost:8083/api/payment/subscription-plans/active`
3. **Headers:**
   - `Content-Type: application/json`

## Notes:
- The `stripePriceId` should be a valid Stripe Price ID from your Stripe dashboard
- The `interval` can be: `day`, `week`, `month`, or `year`
- The `intervalCount` specifies how many intervals (e.g., 2 for every 2 months)
- Currency should be in ISO 4217 format (e.g., "usd", "eur", "gbp")
- Admin endpoint creates plans, frontend endpoint retrieves active plans