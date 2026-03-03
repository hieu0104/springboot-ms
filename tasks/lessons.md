# Lessons Learned — Bugs & Fixes (Senior Mindset)

> Mỗi bug dưới đây đều là lỗi thật trong project. Ghi lại theo format:
> **Lỗi → Root Cause → Fix → Senior sẽ làm gì khác**

---

## Bug #1: `searchProject` trả về empty body

**Triệu chứng:** FE gọi `GET /projects/search?keyword=abc` → nhận `200 OK` nhưng body rỗng.

**Code lỗi:**
```java
// ProjectController.java
List<Project> projects = projectService.searchProjects(keyword, connectedUser);
return new ResponseEntity<>(HttpStatus.OK); // ← BUG: thiếu projects!
```

**Root cause:** Dev quên truyền `projects` vào `ResponseEntity`. Compile vẫn pass vì `new ResponseEntity<>(HttpStatus)` là constructor hợp lệ.

**Fix:**
```java
return new ResponseEntity<>(projects, HttpStatus.OK);
```

**Senior lesson:**
- Code review sẽ bắt lỗi này ngay vì hàm **tính toán xong nhưng không trả kết quả** → red flag.
- Viết **integration test** cho search endpoint thì phát hiện trong 5 phút.
- Dùng `ResponseEntity.ok(projects)` thay vì `new ResponseEntity<>()` — ít khả năng quên truyền body.

---

## Bug #2: FE gửi query param, BE expect @RequestBody

**Triệu chứng:** Upgrade subscription → HTTP `400 Bad Request`.

**Code lỗi:**
```typescript
// FE: subscriptionService.ts
api.post(`/subscriptions/upgrade?planType=${planType}`)  // ← query param

// BE: SubscriptionController.java
public ResponseEntity<String> upgradeSubscription(
    @RequestBody UpgradeSubscriptionRequest request)  // ← expects body
```

**Root cause:** FE dev và BE dev không đồng bộ API contract. Không có API documentation chung.

**Fix:** Sửa FE gửi body thay vì query param:
```typescript
api.post('/subscriptions/upgrade', { planType, provider: 'VNPAY' })
```

**Senior lesson:**
- Dùng **Swagger/OpenAPI** (project đã có!) và generate FE types từ đó → không bao giờ mismatch.
- Viết **contract test** (FE integration test gọi thật vào BE) ở CI pipeline.
- **API-first development:** Thiết kế API spec trước, cả FE và BE code theo spec.

---

## Bug #3: FE gọi 5 endpoint không tồn tại

**Triệu chứng:** Update status/priority/dueDate issue → `404 Not Found`.

**Code lỗi:**
```typescript
// FE gọi:
PUT /issues/{id}/status/{status}     // ← không tồn tại
PUT /issues/{id}/priority/{priority} // ← không tồn tại
PUT /issues/{id}/due-date            // ← không tồn tại
PUT /issues/{id}/description         // ← không tồn tại
PUT /issues/{id}                     // ← BE chỉ có PATCH, không có PUT
```

**BE thật sự có:**
```java
@PatchMapping("/{issueId}")  // ← chỉ có 1 endpoint PATCH
```

**Root cause:** FE thiết kế API calls **mà không kiểm tra BE có endpoint tương ứng hay không**.

**Fix:** Sửa FE dùng `PATCH` cho tất cả:
```typescript
updateStatus:  (id, status) => api.patch(`/issues/${id}`, { status })
updatePriority:(id, priority) => api.patch(`/issues/${id}`, { priority })
```

**Senior lesson:**
- **PATCH = partial update** (gửi field nào update field đó). 1 endpoint đủ cho mọi update.
- **PUT = full replace** (phải gửi toàn bộ object). Dùng khi thay thế hoàn toàn resource.
- Đừng tạo endpoint riêng cho từng field trừ khi có **business logic khác nhau** (ví dụ: transition rule).

---

## Bug #4: Duplicate invitation logic ở 2 controller

**Triệu chứng:** Gọi `/projects/invite` hoặc `/invitations/send` đều hoạt động → code thừa, khó maintain.

**Code lỗi:**
```java
// ProjectController.java
@PostMapping("/invite")  // ← duplicate!
void inviteProject(...) { invitationService.sendInvitation(...); }

// InvitationController.java
@PostMapping("/send")   // ← cũng gọi invitationService!
ApiResponse<Invitation> sendInvitation(...) { ... }
```

**Root cause:** Khi thêm `InvitationController`, dev quên xóa code cũ trong `ProjectController`.

**Fix:** Xóa invitation logic khỏi `ProjectController`, giữ chỉ `InvitationController`.

**Senior lesson:**
- **Single Responsibility:** 1 resource = 1 controller. Invitation = InvitationController.
- Khi refactor (tách module mới) → **grep toàn bộ codebase** tìm code trùng lặp.
- Code review flag: "Tại sao 2 endpoint khác nhau gọi cùng 1 service method?"

---

## Bug #5: Không có transition rule → user nhảy status bất kỳ

**Triệu chứng:** User drag task từ OPEN → CLOSED, bỏ qua IN_PROGRESS và RESOLVED.

**Code lỗi:**
```java
// IssueService.java - updateIssue()
if (request.getStatus() != null) {
    issue.setStatus(request.getStatus()); // ← set bất kỳ status nào!
}
```

**Root cause:** Không có validation logic cho status transition.

**Fix:** Tạo `IssueTransitionRule` (state machine) + endpoint riêng:
```java
public static boolean isAllowed(IssueStatus from, IssueStatus to) {
    // OPEN → IN_PROGRESS ✅
    // OPEN → CLOSED ✅ (cancelled)
    // OPEN → RESOLVED ❌ (phải qua IN_PROGRESS trước)
}
```

**Senior lesson:**
- Mọi entity có **lifecycle** (Issue, Order, Payment, Subscription) đều cần **state machine**.
- Vẽ state diagram **trước khi code** — phát hiện missing transitions, dead states.
- Tách endpoint: `PATCH /issues/{id}` (update data) vs `PATCH /issues/{id}/transition` (chuyển trạng thái có validate).

---

## Bug #6: Race condition — 2 user move cùng 1 issue

**Triệu chứng:** User A drag issue sang IN_PROGRESS, cùng lúc User B drag sang RESOLVED → data inconsistent.

**Root cause:** Không có **optimistic locking**. Cả 2 request đều đọc version cũ → cả 2 đều ghi thành công.

**Fix:** Thêm `@Version` trên entity:
```java
@Version
Long version; // JPA tự kiểm tra version khi save
```
Khi 2 request cùng lúc: request thứ 2 sẽ nhận `OptimisticLockException` → FE biết cần reload.

**Senior lesson:**
- **Bất kỳ field nào 2+ user có thể sửa cùng lúc → cần locking.**
- `@Version` (optimistic) cho hầu hết trường hợp. Pessimistic lock (`SELECT FOR UPDATE`) cho financial transactions.
- Phỏng vấn **luôn hỏi:** "Nếu 2 user thao tác cùng lúc thì sao?" → Đây là câu trả lời.

---

## Bug #7: Response format không nhất quán

**Triệu chứng:** FE phải code 2 kiểu parse response:
```typescript
// UserController → data.result
const user = response.data.result

// IssueController → data trực tiếp  
const issue = response.data
```

**Root cause:** Một số controller dùng `ApiResponse<T>` wrapper, một số trả raw entity.

**Fix:** Wrap **tất cả** controller bằng `ApiResponse<T>`:
```java
return ApiResponse.<Issue>builder().result(issue).build();
```

**Senior lesson:**
- **Consistency > Perfection.** Chọn 1 pattern và áp dụng xuyên suốt.
- FE chỉ cần 1 utility function để parse response thay vì if/else.
- Thiết lập **code template** / archetype cho controller mới → tự động có wrapper.

---

## Bug #8: Test compilation errors đã tồn tại từ trước

**Triệu chứng:** `mvn test` fail ngay cả trước khi sửa code.

**Lỗi 1:** Import sai `com.hieu.ms.feature.user.dto.user.dto.user.dto.UserCreationRequest`
**Lỗi 2:** `User.builder().id()` — `id` nằm ở `BaseEntity`, không có trong `@Builder`

**Root cause:**
- IDE auto-import sai package.
- `@Builder` trên child class không include parent fields.

**Fix:**
```java
// Lỗi 2: dùng setter thay vì builder cho inherited field
user = User.builder().username("john").build();
user.setId("cf0600f538b3");
```

**Senior lesson:**
- **Chạy `mvn test` đầu tiên** khi nhận project mới → biết baseline.
- Nếu test đã fail → sửa test trước rồi mới code tính năng mới.
- `@Builder` + inheritance = trap. Dùng `@SuperBuilder` hoặc setter cho parent fields.

---

## Bug #9: CHECKSUM_BYPASS hardcoded — security bypass ẩn trong production code

**Triệu chứng:** Payment callback chấp nhận bất kỳ request nào gửi `vnp_SecureHash=CHECKSUM_BYPASS`, bỏ qua toàn bộ HMAC validation.

**Code lỗi:**
```java
// VNPayService.java
boolean isValid =
    (signValue != null && signValue.equals(vnp_SecureHash))
    || "CHECKSUM_BYPASS".equals(vnp_SecureHash); // ← backdoor
```

**Root cause:** Dev thêm bypass để test local, **commit lên main**, rồi quên xóa. Không có reviewer nào bắt được vì nó không gây lỗi runtime.

**Fix:**
```java
boolean isValid = signValue != null && signValue.equals(vnp_SecureHash);
```

**Senior lesson:**
- **Không bao giờ commit test shortcut vào security-critical path.** Dùng mock/stub trong test thay vì bypass production logic.
- Static analysis tool (PMD, SonarQube) có rule phát hiện hardcoded secret/bypass string — thiết lập từ đầu project.
- Code review rule: bất kỳ `||` nào trong điều kiện security validation đều phải bị hỏi ngay lập tức.
- Phỏng vấn sẽ hỏi: *"Bạn đảm bảo payment callback không bị giả mạo như thế nào?"* — đây là ví dụ phản diện hoàn hảo.

---

## Bug #10: @EventListener thay vì @TransactionalEventListener — event bắn sai thời điểm

**Triệu chứng:** Payment success event được xử lý **trong cùng transaction** với PaymentService. Nếu PaymentService rollback sau khi event bắn → subscription đã được update nhưng payment thì không → data inconsistent.

**Code lỗi:**
```java
@org.springframework.context.event.EventListener  // ← fires mid-transaction
@Transactional
public void handleSuccessfulPayment(PaymentSuccessEvent event) { ... }
```

**Root cause:** `@EventListener` không quan tâm đến transaction — nó bắn ngay khi `publishEvent()` được gọi, dù transaction chưa commit hay đã rollback.

**Fix:**
```java
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void handleSuccessfulPayment(PaymentSuccessEvent event) { ... }
```

**Senior lesson:**
- **`AFTER_COMMIT`** = listener chỉ chạy khi outer transaction commit thành công → không bao giờ xử lý event của transaction bị rollback.
- **`REQUIRES_NEW`** bắt buộc đi kèm: vì `AFTER_COMMIT` bắn ra ngoài transaction gốc (đã đóng), listener cần mở transaction mới để ghi DB.
- Pattern chuẩn cho event-driven architecture: **publish event → commit → listener chạy trong transaction riêng**.
- Phỏng vấn hỏi *"Event-driven với transactional integrity thế nào?"* → đây là câu trả lời chuẩn senior.

---

## Bug #11: @Version chỉ có trên Issue, thiếu trên Subscription và Project

**Triệu chứng:** 2 admin cùng approve subscription upgrade cho 1 user → cả 2 request đều thành công → user nhận 2 lần upgrade, billing sai. Tương tự: 2 member cùng rename Project → người sau ghi đè người trước không báo lỗi.

**Code lỗi:**
```java
// Issue.java — có @Version ✅
@Version Long version;

// Subscription.java — THIẾU ❌
// Project.java — THIẾU ❌
```

**Root cause:** Dev thêm `@Version` cho Issue vì đọc tài liệu về race condition khi đó, nhưng không apply nhất quán cho các entity khác cùng loại rủi ro.

**Fix:** Thêm `@Version Long version;` vào cả Subscription và Project.

**Senior lesson:**
- **Nguyên tắc:** Bất kỳ entity nào có thể bị 2+ user/process cập nhật đồng thời → **bắt buộc có `@Version`**.
- Checklist khi thiết kế entity mới: *"Entity này có concurrent write risk không?"* → Nếu có, thêm `@Version` ngay từ đầu.
- Optimistic lock (`@Version`) cho hầu hết use case. Pessimistic lock (`SELECT FOR UPDATE`) chỉ dùng cho financial critical path cần đảm bảo tuyệt đối.
- CV nói *"optimistic locking"* mà chỉ 1/3 entity có `@Version` → interviewer hỏi ngay → mất điểm.

---

## Bug #12: CV claim RBAC nhưng không có @PreAuthorize trên bất kỳ endpoint nào

**Triệu chứng:** Bất kỳ user đã login đều có thể gọi `DELETE /users/{id}`, `POST /roles`, `DELETE /roles/{role}` — không có authorization check nào.

**Code lỗi:**
```java
// UserController.java — không có @PreAuthorize
@DeleteMapping("/{userId}")
ApiResponse<String> deleteUser(@PathVariable String userId) { ... }

// RoleController.java — không có @PreAuthorize
@PostMapping
ApiResponse<RoleResponse> create(@RequestBody RoleRequest request) { ... }
```

**Root cause:** Security config chỉ check authentication (đã login chưa), không check authorization (được làm gì). `@EnableMethodSecurity` có thể đã bật nhưng chưa dùng `@PreAuthorize` ở đâu.

**Fix:**
```java
@DeleteMapping("/{userId}")
@PreAuthorize("hasRole('ADMIN')")
ApiResponse<String> deleteUser(@PathVariable String userId) { ... }

@PostMapping
@PreAuthorize("hasRole('ADMIN')")
ApiResponse<RoleResponse> create(@RequestBody RoleRequest request) { ... }
```

**Senior lesson:**
- **Authentication ≠ Authorization.** Security config (`anyRequest().authenticated()`) chỉ là tầng 1. Method-level `@PreAuthorize` là tầng 2.
- Checklist cho mọi endpoint: *"Endpoint này ai được gọi?"* → Nếu không phải tất cả user → thêm `@PreAuthorize`.
- Ưu tiên để `@PreAuthorize` ở **Controller** (gần HTTP layer), không ở Service — dễ audit, dễ đọc security boundary.
- Phỏng vấn test RBAC: *"Nếu tôi dùng token của user thường gọi DELETE /users/admin thì sao?"* → phải trả lời `403 Forbidden` và chỉ đúng chỗ `@PreAuthorize` enforce nó.
