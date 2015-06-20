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
			archive 'target/'
		}
	}
}

stage name: 'Performance', concurrency: concurrentJobs

parallel distributedJobs

node('master') {
	sh 'rm -rf ./*'
  	unarchive mapping: ['target/' : '.']
  	sh 'mkdir -p target/gatling/distributed-results'
  	sh 'num=0; for i in `find -name "*simulation.log"`; do num=$(( num+1 )) ; temp=$(basename $i) cp $i target/gatling/distributed-results/${temp%.log}-$num.log ; done'
  	sh 'sbt generateReport'
}