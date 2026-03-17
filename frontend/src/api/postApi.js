import axiosClient from './axiosClient.js'

/**
 * 게시글 관련 API 함수 모음.
 * axiosClient를 통해 blog-service API를 호출한다.
 */

/**
 * 공개 게시글 목록을 페이지 단위로 조회한다.
 *
 * @param {number} page - 페이지 번호 (0부터 시작)
 * @param {number} size - 페이지당 항목 수
 * @returns {Promise<{content: Array, totalPages: number, totalElements: number}>}
 */
export const getPosts = (page = 0, size = 10) =>
  axiosClient.get('/posts', { params: { page, size, sort: 'createdAt,desc' } })
    .then((res) => res.data)

/**
 * ID로 게시글 단건을 조회한다.
 *
 * @param {number} id - 게시글 ID
 * @returns {Promise<PostResponse>}
 */
export const getPost = (id) =>
  axiosClient.get(`/posts/${id}`).then((res) => res.data)

/**
 * 새 게시글을 생성한다. 로그인 필요.
 *
 * @param {{title: string, content: string, summary: string, published: boolean}} data
 * @returns {Promise<PostResponse>}
 */
export const createPost = (data) =>
  axiosClient.post('/posts', data).then((res) => res.data)

/**
 * 게시글을 수정한다. 작성자 본인만 가능.
 *
 * @param {number} id - 수정할 게시글 ID
 * @param {{title: string, content: string, summary: string, published: boolean}} data
 * @returns {Promise<PostResponse>}
 */
export const updatePost = (id, data) =>
  axiosClient.put(`/posts/${id}`, data).then((res) => res.data)

/**
 * 게시글을 삭제한다. 작성자 본인만 가능.
 *
 * @param {number} id - 삭제할 게시글 ID
 * @returns {Promise<void>}
 */
export const deletePost = (id) =>
  axiosClient.delete(`/posts/${id}`)
