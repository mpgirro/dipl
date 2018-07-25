#
# Round Trip Time (RTT)
#

akka_index_rtt_data <- read.table("akka-index100-rtt.csv", header=T, sep=";")
akka_search_rtt_data <- read.table("akka-search100-rtt.csv", header=T, sep=";")
msa_index_rtt_data <- read.table("msa-index100-rtt.csv", header=T, sep=";")
msa_search_rtt_data <- read.table("msa-search100-rtt.csv", header=T, sep=";")

akka_index_rtt_data$origin <- rep("Akka",nrow(akka_index_rtt_data))
akka_search_rtt_data$origin <- rep("Akka",nrow(akka_search_rtt_data))
msa_index_rtt_data$origin <- rep("MSA",nrow(msa_index_rtt_data))
msa_search_rtt_data$origin <- rep("MSA",nrow(msa_search_rtt_data))

# das hier ist ziemlich sinnlos?
meanMsgL.data <- rbind( akka_index_rtt_data[,c("meanMsgL","origin")], akka_search_rtt_data[,c("meanMsgL","origin")], msa_index_rtt_data[,c("meanMsgL","origin")], msa_search_rtt_data[,c("meanMsgL","origin")]) 
boxplot(meanMsgL ~ origin, data=meanMsgL.data)   

mml_index.data <- rbind( akka_index_rtt_data[,c("meanMsgL","origin")], msa_index_rtt_data[,c("meanMsgL","origin")])
boxplot(meanMsgL ~ origin, 
	data=mml_index.data,
	main = "Index Subsystem - Mean Message Latency",
	xlab = "Architecture",
	ylab = "Mean Message Latency (ms)")  

mml_search.data <- rbind( akka_search_rtt_data[,c("meanMsgL","origin")], msa_search_rtt_data[,c("meanMsgL","origin")]) 
boxplot(meanMsgL ~ origin, 
	data=mml_search.data,
	main = "Search Subsystem - Mean Message Latency",
	xlab = "Architecture",
	ylab = "Mean Message Latency (ms)") 

# -----

ort_index.data <- rbind( akka_index_rtt_data[,c("overallRT","origin")], msa_index_rtt_data[,c("overallRT","origin")])
boxplot(overallRT ~ origin, data=ort_index.data)  

ort_search.data <- rbind( akka_search_rtt_data[,c("overallRT","origin")], msa_search_rtt_data[,c("overallRT","origin")])
boxplot(overallRT ~ origin, data=ort_search.data)  

# -----

meanRTpI_index.data <- rbind( akka_index_rtt_data[,c("meanRTpI","origin")], msa_index_rtt_data[,c("meanRTpI","origin")])
boxplot(meanRTpI ~ origin, data=meanRTpI_index.data)  

#
# Messages Per Second (MPS)
#

akka_index_mps_data <- read.table("akka-index100-mps.csv", header=T, sep=";")
akka_search_mps_data <- read.table("akka-search100-mps.csv", header=T, sep=";")
msa_index_mps_data <- read.table("msa-index100-mps.csv", header=T, sep=";")
msa_search_mps_data <- read.table("msa-search100-mps.csv", header=T, sep=";")

barplot(akka_index_mps_data$mps,
	main = "Akka Index Subsystem - Messages per Second",
	xlab = "Task Unit",
	ylab = "Messages per Second (MpS)",
	names.arg = akka_index_mps_data$task_id,
	col = "darkred")
	
barplot(akka_search_mps_data$mps,
	main = "Akka Search Subsystem - Messages per Second",
	xlab = "Task Unit",
	ylab = "Messages per Second (MpS)",
	names.arg = akka_search_mps_data$task_id,
	col = "darkred")
	
barplot(msa_index_mps_data$mps,
	main = "MSA Index Subsystem - Messages per Second",
	xlab = "Task Unit",
	ylab = "Messages per Second (MpS)",
	names.arg = msa_index_mps_data$task_id,
	col = "darkred")

barplot(msa_search_mps_data$mps,
	main = "MSA Search Subsystem - Messages per Second",
	xlab = "Task Unit",
	ylab = "Messages per Second (MpS)",
	names.arg = msa_search_mps_data$task_id,
	col = "darkred")