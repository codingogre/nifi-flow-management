import groovy.json.JsonBuilder
import groovyx.net.http.RESTClient
import groovy.util.CliBuilder
import static groovy.json.JsonOutput.prettyPrint
import static groovy.json.JsonOutput.toJson
import static groovyx.net.http.ContentType.JSON
import groovy.time.TimeCategory

@Grab(group='org.codehaus.groovy.modules.http-builder',
      module='http-builder',
      version='0.7.1')

cli = new CliBuilder(
    usage:'nifi-flow-scheduler.groovy <OPTIONS> <COMMAND>',
    header: 'Options')
cli.with {
    h(longOpt: 'help', 'Usage Information', required: false)
    p(longOpt: 'processor', args: 1, argName: 'processorId', 'Processor in a process group that will be scheduled to run', required: true)
    n(longOpt: 'host', args: 1, argName: 'hostname', 'Hostname of the NiFi manager', required: true)
    t(longOpt: 'port', args: 1, argName: 'port', 'Port of the NiFi manager', required: false)
    s(longOpt: 'start', 'Start the processor group', required: false)
    //c(longOpt: 'check', 'Check the status of a given flow', required: false)
}

// Parse and check the command line parameters
options = cli.parse(args)
if (!options) System.exit(-1)
if (options.h) cli.usage()

// Set variables to the command line options
host = options.n
port = 8080
processorId = options.p
if (options.t) port = options.t

// If -s or --start was specified then schedule and start flow
if (options.s) {
    //Lookup Processor
    println 'Making sure processor exists...'
	nifi = new RESTClient("http://$host:$port/nifi-api/")
	resp = nifi.get(path: "processors/$processorId")
	assert resp.status == 200
	processGroup = resp.data.status.groupId
	ver = resp.data.revision.version

	println 'Preparing to update the flow state...'
	println 'Stopping the process group to apply changes...'
	def builder = new JsonBuilder()
	builder {
		id "$processGroup"
		state "STOPPED"
	}

	resp = nifi.put(
		path: "flow/process-groups/$processGroup",
		body: builder.toPrettyString(),
		requestContentType: JSON
	)
	assert resp.status == 200

	  // create date to run processor
	use(TimeCategory) {
		runtime = (new Date() + 30.seconds).format("ss mm HH dd MM ?")
	}

	builder {
	    revision {
			clientId "nifi-flow-scheduler.groovy"
			version ver
		}
		component {
			id "$processorId"
			config {
				schedulingPeriod "$runtime"
				schedulingStrategy "CRON_DRIVEN"
			}
		}
	}

	println "Updating processor...\n${builder.toPrettyString()}"

	resp = nifi.put(
		path: "processors/$processorId",
		body: builder.toPrettyString(),
		requestContentType: JSON
	)
	assert resp.status == 200

	println "Updated ok."
	//println "Got this response back:"
	//print prettyPrint(toJson(resp.data))

	println 'Bringing the process group back online...'
	builder {
		id "$processGroup"
		state "RUNNING"
	}

	resp = nifi.put(
		path: "flow/process-groups/$processGroup",
		body: builder.toPrettyString(),
		requestContentType: JSON
	)
	assert resp.status == 200

	println "Flow scheduled successfully"
	System.exit(0)
}
else {
	println "Checking the status of flow"
}
