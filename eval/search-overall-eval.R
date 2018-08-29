akka_search_overall_data <- read.table("../src/benchmark/akka-search-rtt-overall.csv", header=T, sep=";")
msa_search_overall_data <- read.table("../src/benchmark/msa-search-rtt-overall.csv", header=T, sep=";")

# reduce overallRT from milliseconds to seconds 
akka_search_overall_data$overallRT = akka_search_overall_data$overallRT / 1000
msa_search_overall_data$overallRT = msa_search_overall_data$overallRT / 1000

# add colors
akka_search_overall_data$color <- rep("red",nrow(akka_search_overall_data))
msa_search_overall_data$color <- rep("blue",nrow(msa_search_overall_data))


x1 <- akka_search_overall_data$input_size;
y1 <- akka_search_overall_data$overallRT;
x1.lm <- lm(y1~x1);

x2 <- msa_search_overall_data$input_size;
y2 <- msa_search_overall_data$overallRT;
x2.lm <- lm(y2~x2);


# comparison with approximation lines
search_overall_data <- rbind( akka_search_overall_data, msa_search_overall_data);
plot(search_overall_data$input_size,search_overall_data$overallRT, 
     col=search_overall_data$color, pch = 4,
     xlim=c(0,5000), ylim=c(0, 250))
abline(x1.lm, col="red");
abline(x2.lm, col="blue");


# only Akka
plot(x1, y1, 
     type="b", pch=18, col="red", lty=2, cex=1.2, 
     xlab = "Search requests", ylab = "Response Time [ms]",
     xlim=c(0,5000), ylim=c(0, 250))
abline(x1.lm, col="red");

# only MSA
plot(x2, y2, 
     type="b", pch=19, col="blue", lty=2, cex=1.2, 
     xlab = "Search requests", ylab = "Response Time [ms]",
     xlim=c(0,5000), ylim=c(0, 250))
abline(x2.lm, col="blue");



pdf("eval-search-overall.pdf");

plot(x2, y2, type="l", pch=18, col="blue", lty=2, cex=1.2, 
     xlab = "Search Requests", ylab = "Elapsed Time [Seconds]",
     xlim=c(0,5000), ylim=c(0, 250))
lines(x1, y1, type="l", pch=19, col="red")			   
legend("topleft", legend=c("Akka", "MSA"),
       col=c("red", "blue"), lty=1:2, cex=1)

dev.off()