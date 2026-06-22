package com.tourism.dto;

/**
 * 用户资料更新请求数据传输对象（ProfileUpdateRequest DTO）。
 * 
 * <p><strong>DTO 模式说明：</strong></p>
 * <ul>
 *   <li>DTO（Data Transfer Object）用于封装前端提交的数据，避免直接将数据库实体暴露给客户端。</li>
 *   <li>资料更新场景下，前端只需提交需要修改的字段（phone、email、密码变更相关字段），
 *       而不需要传递用户的完整信息（如 id、username、role、createTime 等）。</li>
 *   <li>使用独立的 DTO 可以精确控制允许修改的字段范围，增强系统的安全性与可维护性。</li>
 * </ul>
 * 
 * <p>本类封装了用户修改个人资料时可能提交的数据，包括：</p>
 * <ul>
 *   <li>基础资料：phone（手机号）、email（邮箱）</li>
 *   <li>密码修改：oldPassword（旧密码，用于验证身份）、newPassword（新密码）</li>
 * </ul>
 * <p>密码相关字段为可选：如果用户不修改密码，则 oldPassword 和 newPassword 可以为空。</p>
 */
public class ProfileUpdateRequest {

    /**
     * 新的手机号码，选填。
     * 如果用户希望更新手机号，则提交此字段；否则可为 null。
     */
    private String phone;

    /**
     * 新的电子邮箱，选填。
     * 如果用户希望更新邮箱，则提交此字段；否则可为 null。
     */
    private String email;

    /**
     * 旧密码（当前密码），修改密码时必填。
     * 系统会用此字段验证用户身份，确认是账号持有人在操作，防止他人恶意修改密码。
     * 如果仅更新资料不修改密码，此字段可为空。
     */
    private String oldPassword;

    /**
     * 新密码，修改密码时必填。
     * 验证旧密码通过后，系统会将此密码更新到数据库。
     * 如果仅更新资料不修改密码，此字段可为空。
     */
    private String newPassword;

    /**
     * 获取手机号码。
     *
     * @return 手机号码字符串，可能为 null
     */
    public String getPhone() {
        return phone;
    }

    /**
     * 设置手机号码。
     *
     * @param phone 手机号码字符串，允许为 null
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * 获取电子邮箱。
     *
     * @return 电子邮箱字符串，可能为 null
     */
    public String getEmail() {
        return email;
    }

    /**
     * 设置电子邮箱。
     *
     * @param email 电子邮箱字符串，允许为 null
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * 获取旧密码（当前密码）。
     *
     * @return 旧密码字符串，可能为 null
     */
    public String getOldPassword() {
        return oldPassword;
    }

    /**
     * 设置旧密码（当前密码）。
     *
     * @param oldPassword 旧密码字符串，允许为 null
     */
    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    /**
     * 获取新密码。
     *
     * @return 新密码字符串，可能为 null
     */
    public String getNewPassword() {
        return newPassword;
    }

    /**
     * 设置新密码。
     *
     * @param newPassword 新密码字符串，允许为 null
     */
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
