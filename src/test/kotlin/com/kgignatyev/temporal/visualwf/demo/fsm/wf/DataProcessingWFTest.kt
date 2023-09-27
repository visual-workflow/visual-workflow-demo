package com.kgignatyev.temporal.visualwf.demo.fsm.wf

import com.kgignatyev.temporal.visualwf.demo.fsm.DataProcessingStates
import com.kgignatyev.temporal.visualwf.demo.fsm.FilePointer
import com.kgignatyev.temporal.visualwf.demo.wf.DataProcessingActivities
import com.kgignatyev.temporal.visualwf.demo.wf.DataProcessingWF
import com.kgignatyev.temporal.visualwf.demo.wf.DataProcessingWFImpl
import io.temporal.client.WorkflowClient
import io.temporal.client.WorkflowOptions
import io.temporal.testing.TestWorkflowEnvironment
import io.temporal.worker.Worker
import io.temporal.workflow.Workflow
import org.awaitility.Awaitility
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description


open class DataProcessingWFTest {

    lateinit var testEnv:TestWorkflowEnvironment
    lateinit var wfWorker:Worker
    lateinit var dataProcessingActivity: DataProcessingActivities



    @Rule
    @JvmField  var watchman:TestWatcher = object : TestWatcher() {
        override fun failed(e: Throwable?, description: Description?) {
           if( testEnv != null) {
               error( testEnv.getDiagnostics())
               testEnv.close()
           }
        }
    }

    @Before
    fun setUp(){
        dataProcessingActivity = DataProcessingActivitiesTestImpl()
        testEnv = TestWorkflowEnvironment.newInstance()
        wfWorker = testEnv.newWorker(DataProcessingWFImpl.QUEUE)
        wfWorker.registerWorkflowImplementationTypes(DataProcessingWFImpl::class.java)
        wfWorker.registerActivitiesImplementations(dataProcessingActivity)
        testEnv.start()
    }

    @After
    fun tearDown(){
        testEnv.close()
    }

    @Test
    fun testWorkflow(){
        val client = testEnv.workflowClient
        val wfStub = client.newWorkflowStub(DataProcessingWF::class.java, WorkflowOptions.newBuilder()
            .setTaskQueue(DataProcessingWFImpl.QUEUE)
            .build())
        val wfExecution = WorkflowClient.start(wfStub::processData, "123")
        val wfId = wfExecution.workflowId

        val executionStub = client.newWorkflowStub(DataProcessingWF::class.java, wfId)
        Awaitility.await("Waiting for WaitingForData state").until {
            println(executionStub.workflowInfo.activeStates[0].stateName)
            executionStub.workflowInfo.activeStates.find { it.stateName == DataProcessingStates.WaitingForData.name } != null
        }
        executionStub.dataReceived("file111")
        Awaitility.await("Waiting for DataReview state").until {
            println("dr "+ executionStub.workflowInfo.activeStates[0].stateName)
            executionStub.workflowInfo.activeStates.find { it.stateName == "DataReview" } != null
        }

        executionStub.dataApproved()
        Awaitility.await("Waiting for ProcessData state").until {
            executionStub.workflowInfo.activeStates.find { it.stateName == "ProcessData" } != null
        }
        executionStub.dataProcessed()
        Awaitility.await("Waiting for Done state").until {
            executionStub.workflowInfo.activeStates.find { it.stateName == "Done" } != null
        }
        println("Done")

    }
}


class DataProcessingActivitiesTestImpl: DataProcessingActivities {
    override fun fetchData(filePointer: FilePointer) {
        println("activity: fetchData ${filePointer.fileName}")
    }


}
