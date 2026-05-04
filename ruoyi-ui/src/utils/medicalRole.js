/**
 * 医疗平台登录分流：角色与默认首页
 * 与后端 sys_role.role_key 一致：admin、doctor 等
 */

export function isAdminRole(roles) {
  if (!roles || !roles.length) return false
  return roles.map(role => String(role).toLowerCase()).includes('admin')
}

export function isDoctorRole(roles) {
  if (!roles || !roles.length) return false
  const normalizedRoles = roles.map(role => String(role).toLowerCase())
  return normalizedRoles.some(role => ['doctor', 'physician', 'role_doctor', 'role_physician', '医生', '医师'].includes(role))
}

/** 非管理员且具备医生角色时，进入医生工作台 */
export function isPhysicianPortalUser(roles) {
  return isDoctorRole(roles) && !isAdminRole(roles)
}

/**
 * 登录后默认落地路径（无 redirect 查询参数时由 permission 使用）
 */
export function defaultHomePath(roles) {
  if (isPhysicianPortalUser(roles)) return '/physician/workbench'
  return '/'
}

export function isDefaultEntryPath(path) {
  return path === '/' || path === '/index'
}
