# [Feature] Module Relationship

## 1. Overview (Tổng quan)

- **Mục tiêu:** Cho phép người dùng kết bạn (gửi/nhận yêu cầu), quản lý danh sách bạn bè và chặn (block) người dùng khác.
- **Đối tượng sử dụng:** Tất cả người dùng đã đăng nhập vào hệ thống.

---

## 2. Requirements (Yêu cầu)

- Gửi lời mời kết bạn (Friend Request).
- Chấp nhận hoặc từ chối lời mời kết bạn.
- Hủy kết bạn (Unfriend).
- Chặn (Block) và Bỏ chặn (Unblock) người dùng độc lập với quan hệ bạn bè.
- Xem danh sách bạn bè, danh sách lời mời chờ duyệt và danh sách người bị chặn.

---

## 3. Ràng buộc hệ thống

- **Giới hạn kết bạn:** Mỗi người dùng được phép có tối đa 1,000 bạn bè. Nếu đã đạt giới hạn, không thể gửi thêm lời mời hoặc chấp nhận lời mời mới.
- Phân trang mặc định 20 phần tử mỗi lần truy vấn cho các API lấy danh sách.
- Không thể gửi yêu cầu kết bạn đến người đã block mình hoặc người mà mình đã block.
- Không thể tự gửi lời mời kết bạn cho chính mình.
- **Soft delete:** Khi hủy kết bạn (Unfriend), không xóa dòng dữ liệu mà cập nhật trạng thái (status) thành `UNFRIENDED` để giữ lịch sử phục vụ recommendation/anti-spam. Lọc bỏ trạng thái này khi query danh sách bạn bè.

---

## 4. Data Modelling (Thiết kế dữ liệu)

### 4.1 Nguyên tắc thiết kế

- Tách biệt hoàn toàn hành động kết bạn (Friendship) và hành động chặn (Block) thành 2 bảng khác nhau.
- Bảng `friendships` lưu trạng thái quan hệ 2 chiều, bảng `user_blocks` lưu trạng thái chặn 1 chiều.

### 4.2 Bảng `friendships`

| Column | Type | Constraint | Mô tả |
|---|---|---|---|
| `id` | VARCHAR(36) | PK | Sinh tự động (UUID) |
| `user1_id` | VARCHAR(36) | FK(users.id), NOT NULL | ID người dùng 1 (Luôn nhỏ hơn user2_id theo thứ tự từ điển) |
| `user2_id` | VARCHAR(36) | FK(users.id), NOT NULL | ID người dùng 2 |
| `action_user_id` | VARCHAR(36) | FK(users.id), NOT NULL | ID người thực hiện hành động cuối cùng |
| `status` | VARCHAR(20) | NOT NULL | Enum: PENDING, ACCEPTED, DECLINED, UNFRIENDED |
| `version` | BIGINT | NOT NULL, DEFAULT 0 | Phục vụ Optimistic Locking chống Race Condition |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | |
| `updated_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | |

**Constraints:**
- `uq_friendships_pair`: `UNIQUE(user1_id, user2_id)` — Canonical Ordering chặn dứt điểm trùng lặp 2 chiều.
- `fk_friendships_user1`: Foreign key to `users(id)`
- `fk_friendships_user2`: Foreign key to `users(id)`
- `fk_friendships_action_user`: Foreign key to `users(id)`

**Indexes:**
- `idx_friendships_users`: `(user1_id, user2_id)` — Tối ưu query tìm kiếm mối quan hệ giữa 2 người dùng cụ thể.
- `idx_friendships_user2`: `(user2_id)` — Cover query cho chiều còn lại.

### 4.3 Bảng `user_blocks`

| Column | Type | Constraint | Mô tả |
|---|---|---|---|
| `id` | VARCHAR(36) | PK | Sinh tự động (UUID) |
| `blocker_id` | VARCHAR(36) | FK(users.id), NOT NULL | ID người thực hiện chặn |
| `blocked_id` | VARCHAR(36) | FK(users.id), NOT NULL | ID người bị chặn |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | |

**Constraints:**
- `uq_user_blocks`: `UNIQUE(blocker_id, blocked_id)` — Một người không thể chặn người kia nhiều lần.
- `fk_blocks_blocker`: Foreign key to `users(id)`
- `fk_blocks_blocked`: Foreign key to `users(id)`

**Indexes:**
- `idx_user_blocks_blocker`: `(blocker_id, blocked_id)` — Phục vụ filter nhanh khi chặn tương tác.
- `idx_user_blocks_blocked`: `(blocked_id)` — Để biết 1 user đang bị ai chặn.

---

## 5. API Design

### Quản lý quan hệ (Relationship Management)

| Method | Endpoint | Mô tả | Auth |
|---|---|---|---|
| `POST` | `/api/v1/friendships/requests/{targetUserId}` | Gửi lời mời kết bạn | ✅ |
| `DELETE` | `/api/v1/friendships/requests/{targetUserId}` | Thu hồi lời mời kết bạn (Unsend) | ✅ |
| `PUT` | `/api/v1/friendships/requests/{friendshipId}/accept` | Đồng ý kết bạn | ✅ |
| `PUT` | `/api/v1/friendships/requests/{friendshipId}/decline` | Từ chối kết bạn | ✅ |
| `DELETE` | `/api/v1/friendships/{targetUserId}` | Hủy kết bạn (Soft delete) | ✅ |
| `POST` | `/api/v1/blocks/{targetUserId}` | Chặn người dùng | ✅ |
| `DELETE` | `/api/v1/blocks/{targetUserId}` | Bỏ chặn người dùng | ✅ |
| `GET` | `/api/v1/friendships/friends` | Xem danh sách bạn bè của chính mình | ✅ |
| `GET` | `/api/v1/friendships/users/{targetUserId}/friends` | Xem danh sách bạn bè của người khác | ✅ |
| `GET` | `/api/v1/friendships/status/{targetUserId}` | Lấy trạng thái quan hệ với 1 user | ✅ |
| `GET` | `/api/v1/friendships/requests` | Xem danh sách lời mời (dùng query param `?type=INCOMING\|OUTGOING`) | ✅ |
| `GET` | `/api/v1/blocks` | Xem danh sách người đã chặn | ✅ |



---

## 6. Business Logic & Flow

### 6.1 Gửi lời mời kết bạn (Send Friend Request)

```text
Input: currentUserId (từ JWT), targetUserId (từ URL)

1. Kiểm tra xem targetUserId có tồn tại trong hệ thống không
   └── Thất bại → throw USER_NOT_FOUND

2. Kiểm tra xem currentUserId có trùng với targetUserId không
   └── Thất bại → throw CANNOT_ADD_SELF

3. Kiểm tra giới hạn 1,000 bạn bè của cả hai người dùng
   └── Nếu currentUserId đã đạt giới hạn → throw FRIEND_LIMIT_EXCEEDED
   └── Nếu targetUserId đã đạt giới hạn → throw TARGET_FRIEND_LIMIT_EXCEEDED

4. Kiểm tra quan hệ chặn (block) trong bảng user_blocks
   └── Nếu currentUserId đã chặn targetUserId hoặc ngược lại → throw BLOCKED_BY_USER

5. Kiểm tra quan hệ hiện tại giữa 2 người dùng trong bảng friendships
   └── Nếu status là PENDING và action_user_id == currentUserId → throw FRIEND_REQUEST_ALREADY_SENT
   └── Nếu status là PENDING và action_user_id != currentUserId → throw FRIEND_REQUEST_ALREADY_RECEIVED
   └── Nếu status là ACCEPTED → throw ALREADY_FRIENDS

6. Thực thi hành động chính (Sử dụng Canonical Ordering)
   └── Xác định: `user1 = MIN(current, target)`, `user2 = MAX(current, target)`.
   └── Nếu chưa có dữ liệu: Tạo mới với status = PENDING, action_user_id = currentUserId.
   └── Nếu đã có dữ liệu cũ (UNFRIENDED/DECLINED): Cập nhật status = PENDING, action_user_id = currentUserId, updated_at = NOW.
   └── (Lưu ý: Nếu dữ liệu cũ là PENDING do người kia gửi, hành động này đáng lẽ sẽ trở thành ACCEPTED - nhưng đã được xử lý bằng lỗi FRIEND_REQUEST_ALREADY_RECEIVED ở bước 5 theo yêu cầu).

Output: 200 OK kèm dữ liệu Friendship vừa tạo
```

### 6.2 Chấp nhận lời mời (Accept Request)

```text
Input: currentUserId (từ JWT), relationshipId (từ URL)

1. Tìm kiếm Friendship bằng relationshipId
   └── Nếu không tìm thấy → throw RELATIONSHIP_NOT_FOUND

2. Kiểm tra quyền sở hữu (currentUserId phải là người nhận, không phải người gửi)
   └── Nếu currentUserId == action_user_id → throw NOT_REQUEST_OWNER (Không thể tự chấp nhận lời mời của chính mình)

3. Kiểm tra trạng thái hiện tại
   └── Nếu status != PENDING → throw REQUEST_ALREADY_HANDLED

4. Kiểm tra giới hạn 1,000 bạn bè của cả hai người dùng
   └── Nếu một trong hai đã đạt giới hạn → throw FRIEND_LIMIT_EXCEEDED / TARGET_FRIEND_LIMIT_EXCEEDED

5. Thực thi
   └── Cập nhật status = ACCEPTED, updated_at = NOW
   └── (Sự kiện bất đồng bộ: tăng friendCount cho cả hai người dùng)

Output: 200 OK
```

### 6.3 Từ chối lời mời (Reject Request)

```text
Input: currentUserId (từ JWT), relationshipId (từ URL)

1. Tìm kiếm Friendship bằng relationshipId
   └── Nếu không tìm thấy → throw RELATIONSHIP_NOT_FOUND

2. Kiểm tra quyền sở hữu (currentUserId phải là người nhận)
   └── Nếu currentUserId == action_user_id → throw NOT_REQUEST_OWNER

3. Kiểm tra trạng thái hiện tại
   └── Nếu status != PENDING → throw REQUEST_ALREADY_HANDLED

4. Thực thi
   └── Cập nhật status = DECLINED, action_user_id = currentUserId, updated_at = NOW

Output: 200 OK
```

### 6.4 Thu hồi lời mời (Unsend Request)

```text
Input: currentUserId (từ JWT), targetUserId (từ URL)

1. Tìm kiếm Friendship giữa currentUserId và targetUserId sử dụng Canonical Ordering
   └── Nếu không tìm thấy → throw RELATIONSHIP_NOT_FOUND

2. Kiểm tra vai trò (currentUserId phải là người gửi)
   └── Nếu currentUserId != action_user_id → throw NOT_REQUEST_OWNER

3. Kiểm tra trạng thái hiện tại
   └── Nếu status != PENDING → throw REQUEST_ALREADY_HANDLED

4. Thực thi
   └── Xóa dòng dữ liệu khỏi DB (Hard delete). Vì yêu cầu chưa được chấp nhận nên không cần giữ lịch sử.

Output: 200 OK
```

### 6.5 Hủy kết bạn (Unfriend)

```text
Input: currentUserId (từ JWT), targetUserId (từ URL)

1. Tìm kiếm Friendship sử dụng Canonical Ordering
   └── Nếu không tìm thấy → throw RELATIONSHIP_NOT_FOUND

2. Kiểm tra trạng thái hiện tại
   └── Nếu status != ACCEPTED → throw NOT_FRIENDS

3. Thực thi
   └── Cập nhật status = UNFRIENDED, action_user_id = currentUserId, updated_at = NOW
   └── (Sự kiện bất đồng bộ: giảm friendCount cho cả hai người dùng)

Output: 200 OK
```

### 6.6 Chặn người dùng (Block User)

```text
Input: currentUserId (từ JWT), targetUserId (từ URL)

1. Kiểm tra tự chặn bản thân
   └── Nếu currentUserId == targetUserId → throw CANNOT_ADD_SELF

2. Kiểm tra xem đã chặn chưa
   └── Nếu bảng user_blocks đã có (currentUserId, targetUserId) → throw ALREADY_BLOCKED

3. Thực thi chặn
   └── Insert vào user_blocks: blocker_id = currentUserId, blocked_id = targetUserId

4. Xử lý hệ quả (Xóa bỏ quan hệ hiện tại nếu có)
   └── Truy vấn bảng friendships (sử dụng Canonical Ordering `user1`, `user2`).
   └── Nếu PENDING: Xóa vĩnh viễn (Hard Delete).
   └── Nếu ACCEPTED: Cập nhật status = UNFRIENDED, action_user_id = currentUserId, updated_at = NOW. (Đồng thời giảm friendCount).

Output: 200 OK
```

---

## 7. Security & Validation

| Endpoint | Điều kiện | Rule |
|---|---|---|
| `ALL /api/v1/friendships/*` | Authentication | Bắt buộc phải có token JWT hợp lệ. |
| `ALL /api/v1/blocks/*` | Authentication | Bắt buộc phải có token JWT hợp lệ. |
| `PUT /api/v1/friendships/requests/{id}/*` | Ownership | Chỉ user mang `addressee_id` của request mới được quyền accept/decline. |
| `POST /api/v1/friendships/*` | Input | `targetUserId` không được rỗng và đúng định dạng UUID. |

---

## 8. Error Codes

| Code | HTTP Status | Description |
|---|---|---|
| `USER_NOT_FOUND` | `404` | Target user does not exist or has been deleted |
| `RELATIONSHIP_NOT_FOUND` | `404` | Relationship or friend request not found |
| `CANNOT_ADD_SELF` | `400` | Cannot send a friend request or block yourself |
| `BLOCKED_BY_USER` | `403` | Cannot interact because you blocked them or they blocked you |
| `ALREADY_FRIENDS` | `409` | You are already friends with this user |
| `FRIEND_REQUEST_ALREADY_SENT` | `409` | You have already sent a friend request and it is pending |
| `FRIEND_REQUEST_ALREADY_RECEIVED` | `409` | This user has already sent you a request, please check your pending list |
| `NOT_REQUEST_OWNER` | `403` | You are not authorized to handle (e.g. accept/decline) this request |
| `REQUEST_ALREADY_HANDLED` | `409` | This friend request has already been handled (accepted or declined) |
| `NOT_FRIENDS` | `409` | You are not currently friends (e.g. when trying to unfriend) |
| `ALREADY_BLOCKED` | `409` | You have already blocked this user |
| `FRIEND_LIMIT_EXCEEDED` | `400` | You have reached the maximum number of friends (1000) |
| `TARGET_FRIEND_LIMIT_EXCEEDED` | `400` | Target user has reached their maximum number of friends |
