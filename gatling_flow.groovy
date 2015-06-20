
node('slave') {
	git url: 'https://github.com/meteorfox/gatling-benchmarking.git'
	archive './*'
	env.PATH = "${tool 'SBT'}/bin:${env.PATH}"
	stage 'Performance'
}

def distributedJobs = [:]
for (int i = 0; i < 2; i++) {
	distributedJobs["gatlingLoadClient${i}"] = {
		node('slave') {
			sh 'rm -rf ./*'
			unarchive mapping: ['./*' : '.']
			sh "./loadbalanced_endpoints_workload.sh"
		}
	}
}

parallel distributedJobs
