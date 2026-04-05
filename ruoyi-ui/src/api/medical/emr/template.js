/**
 *电子病历 - 模板接口（UTF-8）
 */
import request from '@/utils/request'

export function listTemplate(query) {
    return request({
        url: '/emr/template/list',
        method: 'get',
        params: query
    })
}

export function getTemplate(id) {
    return request({
        url: '/emr/template/' + id,
        method: 'get'
    })
}

export function addTemplate(data) {
    return request({
        url: '/emr/template',
        method: 'post',
        data: data
    })
}

export function updateTemplate(data) {
    return request({
        url: '/emr/template',
        method: 'put',
        data: data
    })
}

export function delTemplate(ids) {
    return request({
        url: '/emr/template/' + ids,
        method: 'delete'
    })
}