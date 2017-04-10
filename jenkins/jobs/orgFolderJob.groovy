#!groovy

organizationFolder('jcpowermac') {
    description('This contains branch source jobs for GitHub')
    displayName('jcpowermac')
  	orphanedItemStrategy {
        discardOldItems {
		    daysToKeep(0)
		    numToKeep(0)
        }
	}
	organizations {
		github {
            apiUri('https://api.github.com')
			repoOwner('jcpowermac')
			scanCredentialsId("${CRED_ID}")
			pattern('ojalloy')
			checkoutCredentialsId("${CRED_ID}")
			buildOriginBranch(true)
			buildOriginBranchWithPR(true)
			buildOriginPRMerge(false)
			buildOriginPRHead(false)
			buildForkPRMerge(true)
			buildForkPRHead(false)
		}
    }
    triggers {
        periodic(10)
    }
}
