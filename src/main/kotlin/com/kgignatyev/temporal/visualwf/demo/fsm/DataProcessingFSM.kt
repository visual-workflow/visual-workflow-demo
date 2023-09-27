package com.kgignatyev.temporal.visualwf.demo.fsm

import com.kgignatyev.temporal.visualwf.demo.fsm.DataProcessingStates.*
import com.kgignatyev.temporal.visualwf.demo.fsm.DataProcessingEvents.*
import io.jumpco.open.kfsm.stateMachine

interface DataProcessor{
    fun fetchData( filePointer: FilePointer)
}

class DataProcessingFSM( accountID:String, dataProcessor: DataProcessor){

    val fsm = fsmDefinition.create(DataProcessingContext(accountID,dataProcessor), WaitingForData)

    companion object {
        val fsmDefinition = stateMachine(
            DataProcessingStates.entries.toSet(),
            DataProcessingEvents.entries.toSet(),
            DataProcessingContext::class,
            DataProcessingParameters::class
        ){
            defaultInitialState = WaitingForData
            whenState( WaitingForData) {
                onEvent(DataReceived to DataProcessingStates.DataReview) {
                    fetchData( it as FilePointer)
                }
            }

            whenState( DataReview) {
                onEvent(DataApproved to ProcessData) {
                    processData()
                }

                onEvent(DataRejected to WaitingForData) {
                    requestCorrectedData()
                }
            }

            whenState( ProcessData) {
                onEvent(DataProcessed to Done) {

                }
                onEvent( ErrorsFound to ErrorsReview) {
                   notifyReviewers()
                }
            }

            whenState( ErrorsReview) {
                onEvent(CorrectionsAccepted to ProcessCorrections) {
                    processCorrections()
                }
            }

            whenState( ProcessCorrections) {
                onEvent(CorrectionsProcessed to Done) {

                }
            }

        }.build()


    }
}

class DataProcessingContext(val accountID: String,val dataProcessor: DataProcessor){

    fun fetchData( filePointer: FilePointer) {
        println("simulating fetch data from ${filePointer.fileName}")
        dataProcessor.fetchData(filePointer)
        println("simulating data received")
    }

    fun requestCorrectedData() {
        println("simulating request for corrected data")
    }

    fun processData() {
        println("simulating processing data")
    }

    fun notifyReviewers() {
        println("simulating notifying reviewers")

    }

    fun processCorrections() {
        println("simulating processing corrections")
    }

}


enum class DataProcessingStates {
    WaitingForData,
    DataReview,
    ProcessData,
    ErrorsReview,
    ProcessCorrections,
    Done
}

enum class DataProcessingEvents {
    DataReceived,
    DataApproved,
    DataRejected,
    DataProcessed,
    ErrorsFound,
    CorrectionsAccepted,
    CorrectionsProcessed
}

sealed class DataProcessingParameters

//we do not use data class to allow us use default serializer in Temporal's java SDK
class FilePointer():DataProcessingParameters(){

    constructor(fileName:String) : this() {
        this.fileName = fileName
    }
    var fileName:String = ""

}
