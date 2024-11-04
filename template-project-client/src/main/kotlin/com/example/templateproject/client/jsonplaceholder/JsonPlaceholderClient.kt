package com.example.templateproject.client.jsonplaceholder

import com.example.templateproject.client.jsonplaceholder.api.Post
import com.example.templateproject.client.jsonplaceholder.api.User
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.DeleteExchange
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.PostExchange
import org.springframework.web.service.annotation.PutExchange

const val USERS_ENDPOINT = "/users"
const val POSTS_ENDPOINT = "/posts"

interface JsonPlaceholderClient {

    @GetExchange(USERS_ENDPOINT)
    fun getUsers(): ResponseEntity<List<User>>

    @GetExchange(POSTS_ENDPOINT)
    fun getAllPostByUserId(@RequestParam userId: Long): ResponseEntity<List<Post>>

    @GetExchange("$POSTS_ENDPOINT/{id}")
    fun findPostById(@PathVariable id: Long): ResponseEntity<Post>

    @PostExchange(POSTS_ENDPOINT)
    fun createPost(@RequestBody post: Post): ResponseEntity<Post?>

    @PutExchange("$POSTS_ENDPOINT/{id}")
    fun updatePost(@PathVariable id: Long, post: Post): ResponseEntity<Post>

    @DeleteExchange("$POSTS_ENDPOINT/{id}")
    fun deletePost(@PathVariable id: Long): ResponseEntity<Any?>
}
