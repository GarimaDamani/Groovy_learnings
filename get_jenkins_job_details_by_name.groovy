// Name: get_job_details_by_name.groovy
// Author: Garima Damani
// ---------------------------------------------------------------
// This script gets the below job details with the given job name:
//  *Full name
//  *Last build number
//  *Last build time
//  *Last build cause
//  *Last success build number
//  *Last result
//  *Get recipient list
// ---------------------------------------------------------------


import hudson.model.*
import hudson.maven.*
import hudson.tasks.*
import jenkins.model.Jenkins
import hudson.maven.reporters.*
import hudson.plugins.emailext.*

name = "test_master_builder_project"
//If we want to add more then one job
def items = new LinkedHashSet();
def job = Hudson.instance.getJob(name)
items.add(job);

def findCause(upStreamBuild) {
    //Check if the build was triggered by SCM change
    scmCause = upStreamBuild.getCause(hudson.triggers.SCMTrigger.SCMTriggerCause)
    if (scmCause != null) {
        return scmCause.getShortDescription()
    }
    
    //Check if the build was triggered by timer
    timerCause = upStreamBuild.getCause(hudson.triggers.TimerTrigger.TimerTriggerCause)
    if (timerCause != null) {
        return timerCause.getShortDescription()
    }
    
    //Check if the build was triggered by some jenkins user
    usercause = upStreamBuild.getCause(hudson.model.Cause.UserIdCause.class)
    if (usercause != null) {
        return usercause.getUserId()
    }
    
    //Check if the build was triggered by some jenkins project(job)
    upstreamcause = upStreamBuild.getCause(hudson.model.Cause.UpstreamCause.class)
    if (upstreamcause != null) {
        job = Jenkins.getInstance().getItemByFullName(upstreamcause.getUpstreamProject(), hudson.model.Job.class)
        if (job != null) {
            upstream = job.getBuildByNumber(upstreamcause.getUpstreamBuild())
            if (upstream != null) {
                return upstream
            }
        }
    }
    return;
}

// Iterate through each item.
items.each { item ->
    try {
        def job_data = Jenkins.instance.getItemByFullName(item.fullName)
        println 'Job: ' + item.fullName
        
        //Check if job had atleast one build done
        if (job_data.getLastBuild()) {
            last_job_num = job_data.getLastBuild().getNumber()
            def upStreamBuild = Jenkins.getInstance().getItemByFullName(item.fullName).getBuildByNumber(last_job_num)
            
            println 'LastBuildNumer: ' + last_job_num
            println "LastBuildTime: ${upStreamBuild.getTime()}"
            println 'LastBuildCause: ' + findCause(upStreamBuild)
            
            //Check if job had atleast one successful build
            if (job_data.getLastSuccessfulBuild()) {
                println 'LastSuccessNumber: ' + job_data.getLastSuccessfulBuild().getNumber()
                println 'LastSuccessResult: ' + job_data.getLastSuccessfulBuild().result
            } else {
                println 'LastSuccessNumber: Null'
                println 'LastSuccessResult: Null'
            }
        } else {
            println 'LastBuildNumer: Null'
        }
        //Get the list of recipient
        if(!(item instanceof ExternalJob)) {
            if(item instanceof MavenModuleSet) {
                for(reporter in item.reporters) {
                    if(reporter instanceof MavenMailer) {
                        if(reporter.recipients) {
                            println 'recipients: ' + reporter.recipients
                        } else {
                            println 'recipients: Null'
                        }
                    }
                }
            }
            
            for(publisher in item.publishersList) {
                if(publisher instanceof Mailer) {
                    if(publisher.recipients) {
                        println  'recipients: ' + publisher.recipients
                    } else {
                        println 'recipients: Null'
                    }
                }
                else if(publisher instanceof ExtendedEmailPublisher) {
                    if(publisher.recipientList) {
                        println  'recipients: ' + publisher.recipientList
                    } else {
                        println 'recipients: Null'
                    }
                }
            }
        }
    } catch (Exception e) {
        println ' Ignoring exception ' + e
    }
}
