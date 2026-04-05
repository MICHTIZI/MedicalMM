/**
 * 电子病历 - 接口文件（UTF-8）
 */
import request from '@/utils/request'

export function listRecord(query) {
    return request({
        url: '/emr/record/list',
        method: 'get',
        params: query
    })
}

export function getRecord(id) {
    return request({
        url: '/emr/record/' + id,
        method: 'get'
    })
}

export function addRecord(data) {
    return request({
        url: '/emr/record',
        method: 'post',
        data: data
    })
}

export function updateRecord(data) {
    return request({
        url: '/emr/record',
        method: 'put',
        data: data
    })
}

export function delRecord(id) {
    return request({
        url: '/emr/record/' + id,
        method: 'delete'
    })
}

export function extractEntities(content) {
    return request({
        url: '/emr/record/extract',
        method: 'post',
        data: { content }
    })
}

export function archiveRecord(id) {
    return request({
        url: '/emr/record/archive/' + id,
        method: 'put'
    })
}

export function unarchiveRecord(id) {
    return request({
        url: '/emr/record/unarchive/' + id,
        method: 'put'
    })
}