import axiosClient from './axiosClient.js'

/**
 * 아이템 목록 조회 (필터링 + 페이지네이션).
 *
 * @param {{ keyword?, itemType?, server?, minPrice?, maxPrice?, page?, size?, sort? }} params
 */
export const getItems = (params = {}) =>
  axiosClient.get('/items', { params }).then((res) => res.data)

/**
 * 아이템 단건 조회.
 *
 * @param {number} id
 */
export const getItem = (id) =>
  axiosClient.get(`/items/${id}`).then((res) => res.data)

/**
 * 아이템 등록.
 *
 * @param {{ server, sellerName, itemType, title, price, quantity }} data
 */
export const createItem = (data) =>
  axiosClient.post('/items', data).then((res) => res.data)

/**
 * 아이템 수정 (서버/판매자명 변경 불가).
 *
 * @param {number} id
 * @param {{ title, itemType, price, quantity }} data
 */
export const updateItem = (id, data) =>
  axiosClient.put(`/items/${id}`, data).then((res) => res.data)

/**
 * 아이템 삭제.
 *
 * @param {number} id
 */
export const deleteItem = (id) =>
  axiosClient.delete(`/items/${id}`)
