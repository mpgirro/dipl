# 
# Boxplot describing the distribution of overall roundtrip times for 5k retrievals
# Comparing both Akka and MSA in one image
#
akka_search10000_rtt_progress <- read.table("../src/benchmark/data/akka-search5000-rtt-progress.csv", header=T, sep=";");
msa_search10000_rtt_progress <- read.table("../src/benchmark/data/msa-search5000-rtt-progress.csv", header=T, sep=";");

# add a column stating where the data comes from, for later descrimination
akka_search10000_rtt_progress$origin <- rep("Akka",nrow(akka_search10000_rtt_progress));
msa_search10000_rtt_progress$origin <- rep("MSA",nrow(msa_search10000_rtt_progress));

# draw boxplot comparing the mean latency per message, that is the timespan between sending and receiving
meanMsgL_search.data <- rbind( akka_search10000_rtt_progress[,c("meanMsgL","origin")], msa_search10000_rtt_progress[,c("meanMsgL","origin")]); 
boxplot(meanMsgL ~ origin, data=meanMsgL_search.data, outline=FALSE);  

# draw boxplot comparing the overall runtime, that is from the point the message was received until the answer was returned
overallRT_search.data <- rbind( akka_search10000_rtt_progress[,c("overallRT","origin")], msa_search10000_rtt_progress[,c("overallRT","origin")]); 
boxplot(overallRT ~ origin, 
	data=overallRT_search.data, 
	outline=FALSE,
	main = "Overall Round Trip Time for Search",
	ylab = "Round Trip Time [ms]");  