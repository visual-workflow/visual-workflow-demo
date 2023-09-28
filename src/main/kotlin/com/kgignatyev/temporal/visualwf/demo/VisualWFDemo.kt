package com.kgignatyev.temporal.visualwf.demo

import org.springframework.boot.SpringApplication

import org.springframework.boot.autoconfigure.SpringBootApplication




@SpringBootApplication
class VisualWFDemo {


    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(VisualWFDemo::class.java, *args).start()
        }
    }
}

