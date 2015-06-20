stage name: 'Gatling Setup', concurrency: 1

node('slave') {
	git url: 'https://github.com/meteorfox/gatling-benchmarking.git'
	archive 'build.sbt, loadbalanced_endpoints_workload_debug.sh, project/, src/'
	env.PATH = "${tool 'SBT'}/bin:${env.PATH}"
}

def concurrentJobs = 2
def distributedJobs = [:]
for (int i = 0; i < concurrentJobs; i++) {
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
			archive 'target/, project/, build.sbt, src/'
		}
	}
}

stage name: 'Performance', concurrency: concurrentJobs

parallel distributedJobs

node('master') {
	env.PATH = "${tool 'SBT'}/bin:${env.PATH}"
	sh 'rm -rf ./*'
  	unarchive mapping: [
  		'target/' : '.',
  		'project/' : '.',
  		'build.sbt' : '.',
  		'src/' : '.'
  	]
  	def nowMillis = System.currentTimeMillis()
  	sh "mkdir -p target/gatling/distributedresults-${nowMillis}/"
  	sh "num=0; for i in `find -name \"*simulation.log\"`; do num=$(( num+1 )) ; cp $i target/gatling/distributed-${nowMillis}/simulation$num.log ; done"
  	sh "sbt 'generateReport distributedresults-${nowMillis}'"
  	archive "target/gatling/distributedresults-${nowMillis}/"
}