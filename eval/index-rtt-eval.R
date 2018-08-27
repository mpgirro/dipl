
# 
# Boxplot describing the distribution of overall roundtrip times for 200 feeds with 70 items each
# Comparing both Akka and MSA in one image
#
akka_index10000_rtt_progress <- read.table("../src/benchmark/data/akka-index-i70-f200-rtt-progress.csv", header=T, sep=";");
msa_index10000_rtt_progress <- read.table("../src/benchmark/data/msa-index-i70-f200-rtt-progress.csv", header=T, sep=";");

# add a column stating where the data comes from, for later descrimination
akka_index10000_rtt_progress$origin <- rep("Akka",nrow(akka_index10000_rtt_progress));
msa_index10000_rtt_progress$origin <- rep("MSA",nrow(msa_index10000_rtt_progress));

# draw boxplot comparing the mean latency per message, that is the timespan between sending and receiving
meanMsgL_index.data <- rbind( akka_index10000_rtt_progress[,c("meanMsgL","origin")], msa_index10000_rtt_progress[,c("meanMsgL","origin")]); 
boxplot(meanMsgL ~ origin, data=meanMsgL_index.data, outline=FALSE);  

# draw boxplot comparing the overall runtime, that is from the point the message was received until the answer was returned
overallRT_index.data <- rbind( akka_index10000_rtt_progress[,c("overallRT","origin")], msa_index10000_rtt_progress[,c("overallRT","origin")]); 
boxplot(overallRT ~ origin, data=overallRT_index.data, outline=FALSE); 