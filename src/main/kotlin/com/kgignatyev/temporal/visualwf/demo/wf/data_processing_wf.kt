package com.kgignatyev.temporal.visualwf.demo.wf

import com.kgignatyev.temporal.visualwf.api.VisualWF
import com.kgignatyev.temporal.visualwf.api.WFInfo
import com.kgignatyev.temporal.visualwf.api.WFStateInfo
import com.kgignatyev.temporal.visualwf.demo.fsm.*
import io.temporal.activity.ActivityInterface
import io.temporal.activity.ActivityOptions
import io.temporal.common.RetryOptions
import io.temporal.spring.boot.WorkflowImpl
import io.temporal.workflow.SignalMethod
import io.temporal.workflow.Workflow
import io.temporal.workflow.WorkflowInterface
import io.temporal.workflow.WorkflowMethod
import org.springframework.stereotype.Component
import java.time.Duration


@ActivityInterface
interface DataProcessingActivities:DataProcessor {

}

@WorkflowInterface
interface DataProcessingWF:VisualWF{

    @WorkflowMethod
    fun processData(accountId:String)

    @SignalMethod
    fun dataReceived(filePointer: String?)
    @SignalMethod
    fun dataApproved()
    @SignalMethod
    fun dataRejected()
    @SignalMethod
    fun errorsFound()
    @SignalMethod
    fun correctionsAccepted()
    @SignalMethod
    fun correctionsProcessed()
    @SignalMethod
    fun dataProcessed()

}

@WorkflowImpl(taskQueues = [DataProcessingWFImpl.QUEUE])
class DataProcessingWFImpl:DataProcessingWF{

    companion object {
        const val QUEUE = "data-processing-wf"
    }

    lateinit var fsm: DataProcessingFSM
    lateinit var accountId:String

    val dataProcessingActivitiesOptions = ActivityOptions.newBuilder()
        .setTaskQueue(QUEUE)
        .setScheduleToCloseTimeout(Duration.ofSeconds(3))
        .setRetryOptions( RetryOptions.newBuilder().setMaximumAttempts(1).build())
        .build()

    val dataProcessingActivities =
        Workflow.newActivityStub(DataProcessingActivities::class.java, dataProcessingActivitiesOptions)



    @WorkflowMethod
    override fun processData(accountId:String){
        this.accountId = accountId
        fsm =  DataProcessingFSM(accountId,dataProcessingActivities)
        Workflow.await { fsm.fsm.currentState.name == DataProcessingStates.Done.name }
    }

    @SignalMethod
    override fun dataReceived(filePointer: String?){
        if( filePointer != null) {
            fsm.fsm.sendEvent(DataProcessingEvents.DataReceived, FilePointer(filePointer))
        }else{
            Workflow.getLogger(this::class.java).error("received file pointer is null")
        }
    }

    @SignalMethod
    override fun dataApproved(){
        fsm.fsm.sendEvent(DataProcessingEvents.DataApproved)
    }

    @SignalMethod
    override fun dataRejected(){
        fsm.fsm.sendEvent(DataProcessingEvents.DataRejected)
    }

    @SignalMethod
    override fun errorsFound(){
        fsm.fsm.sendEvent(DataProcessingEvents.ErrorsFound)
    }
    @SignalMethod
    override fun correctionsAccepted(){
        fsm.fsm.sendEvent(DataProcessingEvents.CorrectionsAccepted)
    }
    @SignalMethod
    override fun correctionsProcessed(){
        fsm.fsm.sendEvent(DataProcessingEvents.CorrectionsProcessed)
    }

    @SignalMethod
    override fun dataProcessed(){
        fsm.fsm.sendEvent(DataProcessingEvents.DataProcessed)
    }


    override fun getPlantUMLWorkflowDefinition(): String {
        //read resource into string
        val uml = this::class.java.getResource("/data-processing-wf.plantuml")?.readText()
        return uml ?: "unable to read resource"
    }

    override fun getWorkflowInfo(): WFInfo {
        return  WFInfo().setLegend("Account: ${accountId}")
            .setActiveStates(listOf( WFStateInfo().setStateName(fsm.fsm.currentState.name)))
    }

}
