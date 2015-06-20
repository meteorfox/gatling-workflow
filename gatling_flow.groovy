
node('slave') {
	git url: 'https://github.com/meteorfox/gatling-benchmarking.git'
	env.PATH = "${tool 'SBT'}:${env.PATH}"
    echo "${env.PATH}"
	stage 'Performance'
}

def distributedJobs = [:]
for (int i = 0; i < 2; i++) {
	distributedJobs["gatlingLoadClient${i}"] = {
		node('slave') {
			sh 'ls'
			sh "./loadbalanced_endpoints_workload.sh"
		}
	}
}

parallel distributedJobs
