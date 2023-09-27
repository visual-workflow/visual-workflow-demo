package com.kgignatyev.temporal.visualwf.demo.fsm

import org.junit.Test
import com.kgignatyev.temporal.visualwf.demo.fsm.DataProcessingEvents.*
import io.jumpco.open.kfsm.StateMachineInstance


class DataProcessingFSMTest {

    val mockDataProcessor:DataProcessor = object : DataProcessor {
        override fun fetchData(filePointer: FilePointer) {
            println("fetching data for ${filePointer.fileName}")
        }


    }

    @Test
    fun testFSM_happyPath() {
        val fsm = DataProcessingFSM("123",mockDataProcessor)
        describeFSM(fsm.fsm)
        fsm.fsm.sendEvent(DataReceived,FilePointer("file1"))
        describeFSM(fsm.fsm)
        fsm.fsm.sendEvent(DataApproved)
        describeFSM(fsm.fsm)
        fsm.fsm.sendEvent(DataProcessed)
        describeFSM(fsm.fsm)
    }

    private fun describeFSM(fsm: StateMachineInstance<DataProcessingStates, DataProcessingEvents, DataProcessingContext, DataProcessingParameters, Any>) {
        println("FSM: ${fsm.currentState.name}")
    }

    @Test
    fun testFSM_errorPath() {
        val fsm = DataProcessingFSM("123",mockDataProcessor)
        fsm.fsm.sendEvent(DataReceived,FilePointer("file1"))
        describeFSM(fsm.fsm)
        fsm.fsm.sendEvent(DataApproved)
        describeFSM(fsm.fsm)
        fsm.fsm.sendEvent(ErrorsFound)
        describeFSM(fsm.fsm)
        fsm.fsm.sendEvent(CorrectionsAccepted)
        describeFSM(fsm.fsm)
        fsm.fsm.sendEvent(CorrectionsProcessed)
        describeFSM(fsm.fsm)
    }
}
