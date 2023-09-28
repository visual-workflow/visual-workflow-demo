package com.kgignatyev.temporal.visualwf.demo.activities

import com.kgignatyev.temporal.visualwf.demo.fsm.FilePointer
import com.kgignatyev.temporal.visualwf.demo.wf.DataProcessingActivities
import com.kgignatyev.temporal.visualwf.demo.wf.DataProcessingWFImpl
import io.temporal.spring.boot.ActivityImpl
import org.springframework.stereotype.Component

@Component
@ActivityImpl(taskQueues = [DataProcessingWFImpl.QUEUE])
class DataProcessingActivitiesImpl: DataProcessingActivities {
    override fun fetchData(filePointer: FilePointer) {
        println("fetching data for file pointer $filePointer - 'real' activity")
    }
}
