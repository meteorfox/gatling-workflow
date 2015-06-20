
node('slave') {
	git url: 'https://github.com/meteorfox/gatling-benchmarking.git'
	archive 'build.sbt, loadbalanced_endpoints_workload_debug.sh, project/, src/'
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
				'loadbalanced_endpoints_workload_debug.sh' : '.'
			]
			sh "chmod +x ./loadbalanced_endpoints_workload_debug.sh"
			sh "./loadbalanced_endpoints_workload_debug.sh"
			archive 'target/'
		}
	}
}

parallel distributedJobs

node('slave') {
  	unarchive mapping: ['target/' : '.']
}