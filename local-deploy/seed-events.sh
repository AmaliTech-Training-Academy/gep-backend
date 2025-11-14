#!/bin/bash

echo "🌱 Seeding Events via API..."
echo ""

# Login and get token
echo "1. Logging in as bob.anderson2@example.com..."
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "bob.anderson2@example.com", "password": "Test123!"}')

sleep 2

echo "2. Getting OTP from logs..."
OTP=$(docker logs gep-auth-service 2>&1 | grep -oE '"otp":"[0-9]{6}"' | tail -1 | grep -oE '[0-9]{6}')

if [ -z "$OTP" ]; then
    echo "❌ Could not find OTP in logs"
    echo "Please run manually:"
    echo "  docker logs gep-auth-service | grep -i otp"
    exit 1
fi

echo "   OTP: $OTP"

echo "3. Verifying OTP..."
TOKEN_RESPONSE=$(curl -s -X POST http://localhost:8081/api/v1/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d "{\"email\": \"bob.anderson2@example.com\", \"otp\": \"$OTP\"}")

TOKEN=$(echo $TOKEN_RESPONSE | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
    echo "❌ Could not get access token"
    echo "Response: $TOKEN_RESPONSE"
    exit 1
fi

echo "   ✅ Got access token"
echo ""

# Create events
echo "4. Creating 20 events..."

EVENTS=(
  '{"title":"Tech Conference 2025","description":"Annual technology conference featuring AI and Cloud innovations","eventTime":"2025-03-15T09:00:00","startTime":"2025-03-15T09:00:00","endTime":"2025-03-15T18:00:00","location":"Moscone Center, San Francisco","flyerUrl":"https://picsum.photos/400/300?random=1"}'
  '{"title":"Startup Pitch Night","description":"Watch innovative startups pitch to investors","eventTime":"2025-02-20T18:00:00","startTime":"2025-02-20T18:00:00","endTime":"2025-02-20T21:00:00","location":"WeWork Mission St, San Francisco","flyerUrl":"https://picsum.photos/400/300?random=2"}'
  '{"title":"AI Workshop","description":"Hands-on workshop building LLM applications","eventTime":"2025-03-01T10:00:00","startTime":"2025-03-01T10:00:00","endTime":"2025-03-01T16:00:00","location":"TechHub Market St, San Francisco","flyerUrl":"https://picsum.photos/400/300?random=3"}'
  '{"title":"Networking Mixer","description":"Connect with tech professionals","eventTime":"2025-02-28T17:00:00","startTime":"2025-02-28T17:00:00","endTime":"2025-02-28T20:00:00","location":"The View Lounge, San Francisco","flyerUrl":"https://picsum.photos/400/300?random=4"}'
  '{"title":"Hackathon 2025","description":"24-hour hackathon for social impact","eventTime":"2025-04-05T09:00:00","startTime":"2025-04-05T09:00:00","endTime":"2025-04-06T09:00:00","location":"GitHub HQ, San Francisco","flyerUrl":"https://picsum.photos/400/300?random=5"}'
  '{"title":"Cloud Architecture Masterclass","description":"Deep dive into cloud architecture patterns","eventTime":"2025-03-10T09:00:00","startTime":"2025-03-10T09:00:00","endTime":"2025-03-10T17:00:00","location":"AWS Office, San Francisco","flyerUrl":"https://picsum.photos/400/300?random=6"}'
  '{"title":"Women in Tech Summit","description":"Celebrating women in technology","eventTime":"2025-05-08T08:00:00","startTime":"2025-05-08T08:00:00","endTime":"2025-05-08T19:00:00","location":"Palace Hotel, San Francisco","flyerUrl":"https://picsum.photos/400/300?random=7"}'
  '{"title":"Blockchain Meetup","description":"Exploring Web3 and decentralized apps","eventTime":"2025-02-25T18:30:00","startTime":"2025-02-25T18:30:00","endTime":"2025-02-25T21:00:00","location":"Coinbase Office, San Francisco","flyerUrl":"https://picsum.photos/400/300?random=8"}'
  '{"title":"DevOps Best Practices","description":"Modern DevOps and CI/CD pipelines","eventTime":"2025-03-20T10:00:00","startTime":"2025-03-20T10:00:00","endTime":"2025-03-20T15:00:00","location":"Docker HQ, San Francisco","flyerUrl":"https://picsum.photos/400/300?random=9"}'
  '{"title":"Product Management Workshop","description":"From idea to launch","eventTime":"2025-04-12T09:00:00","startTime":"2025-04-12T09:00:00","endTime":"2025-04-12T17:00:00","location":"Airbnb Office, San Francisco","flyerUrl":"https://picsum.photos/400/300?random=10"}'
  '{"title":"Cybersecurity Conference","description":"Latest trends in cybersecurity","eventTime":"2025-05-15T08:00:00","startTime":"2025-05-15T08:00:00","endTime":"2025-05-15T18:00:00","location":"Marriott Marquis, San Francisco","flyerUrl":"https://picsum.photos/400/300?random=11"}'
  '{"title":"Mobile App Bootcamp","description":"Build iOS and Android apps","eventTime":"2025-03-25T09:00:00","startTime":"2025-03-25T09:00:00","endTime":"2025-03-27T17:00:00","location":"General Assembly, San Francisco","flyerUrl":"https://picsum.photos/400/300?random=12"}'
  '{"title":"Data Science Symposium","description":"Machine learning and analytics","eventTime":"2025-04-18T09:00:00","startTime":"2025-04-18T09:00:00","endTime":"2025-04-18T18:00:00","location":"Salesforce Tower, San Francisco","flyerUrl":"https://picsum.photos/400/300?random=13"}'
  '{"title":"UX/UI Design Sprint","description":"Intensive design thinking workshop","eventTime":"2025-03-08T09:00:00","startTime":"2025-03-08T09:00:00","endTime":"2025-03-09T17:00:00","location":"IDEO Office, San Francisco","flyerUrl":"https://picsum.photos/400/300?random=14"}'
  '{"title":"Founders Dinner","description":"Exclusive dinner for startup founders","eventTime":"2025-04-22T19:00:00","startTime":"2025-04-22T19:00:00","endTime":"2025-04-22T22:00:00","location":"Quince Restaurant, San Francisco","flyerUrl":"https://picsum.photos/400/300?random=15"}'
  '{"title":"Open Source Day","description":"Contribute to open source projects","eventTime":"2025-03-30T10:00:00","startTime":"2025-03-30T10:00:00","endTime":"2025-03-30T18:00:00","location":"Mozilla Office, San Francisco","flyerUrl":"https://picsum.photos/400/300?random=16"}'
  '{"title":"Quantum Computing Seminar","description":"Introduction to quantum computing","eventTime":"2025-05-20T14:00:00","startTime":"2025-05-20T14:00:00","endTime":"2025-05-20T17:00:00","location":"IBM Research, San Jose","flyerUrl":"https://picsum.photos/400/300?random=17"}'
  '{"title":"Agile Workshop","description":"Transform with agile methodologies","eventTime":"2025-04-08T09:00:00","startTime":"2025-04-08T09:00:00","endTime":"2025-04-08T16:00:00","location":"Atlassian Office, San Francisco","flyerUrl":"https://picsum.photos/400/300?random=18"}'
  '{"title":"Tech Career Fair","description":"Meet top tech companies","eventTime":"2025-05-25T10:00:00","startTime":"2025-05-25T10:00:00","endTime":"2025-05-25T16:00:00","location":"Fort Mason Center, San Francisco","flyerUrl":"https://picsum.photos/400/300?random=19"}'
  '{"title":"API Design Workshop","description":"RESTful and GraphQL API design","eventTime":"2025-04-15T10:00:00","startTime":"2025-04-15T10:00:00","endTime":"2025-04-15T15:00:00","location":"Postman Office, San Francisco","flyerUrl":"https://picsum.photos/400/300?random=20"}'
)

COUNT=0
for event in "${EVENTS[@]}"; do
  RESPONSE=$(curl -s -X POST http://localhost:8082/api/v1/events \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "$event")
  
  if echo "$RESPONSE" | grep -q "id"; then
    COUNT=$((COUNT + 1))
    echo "   ✅ Event $COUNT created"
  else
    echo "   ❌ Failed to create event: $RESPONSE"
  fi
done

echo ""
echo "✅ Seeding complete! Created $COUNT events"
echo ""
echo "Test it:"
echo "  curl http://localhost:8082/api/v1/events/explore"
