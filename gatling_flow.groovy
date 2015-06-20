
node('slave') {
	env.PATH = "${tool 'SBT'}/bin:${env.PATH}"
	stage 'Performance'
}

def distributedJobs = [:]
for (int i = 0; i < 2; i++) {
	distributedJobs["gatlingLoadClient${i}"] = {
		node('slave') {
			git url: 'https://github.com/meteorfox/gatling-benchmarking.git'
			sh "./loadbalanced_endpoints_workload.sh"
		}
	}
}

parallel distributedJobs
