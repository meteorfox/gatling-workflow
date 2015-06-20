
node('slave') {
	git url: 'https://github.com/meteorfox/gatling-benchmarking.git'
	archive 'build.sbt, loadbalanced_endpoints_workload.sh, project/, src/'
	env.PATH = "${tool 'SBT'}/bin:${env.PATH}"
	stage 'Performance'
}

def distributedJobs = [:]
for (int i = 0; i < 2; i++) {
	distributedJobs["gatlingLoadClient${i}"] = {
		node('slave') {
			sh 'rm -rf ./*'
			unarchive mapping: [
				'project/' : '.',
				'src/' : '.',
				'build.sbt' : '.',
				'loadbalanced_endpoints_workload.sh' : '.'
			]
			sh "chmod +x ./loadbalanced_endpoints_workload.sh"
			sh "./loadbalanced_endpoints_workload.sh"
		}
	}
}

parallel distributedJobs
