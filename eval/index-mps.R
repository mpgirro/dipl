index_mps <- read.table("data/index-mps.csv", header=T, sep=";");

r_vars <- rbind(index_mps$akka, index_mps$msa)

#par(mar=c(7,4,4,2))
barplot(r_vars, beside=TRUE, col=c("red","blue"), las=2,
        names.arg=index_mps$component, ylim=c(0, 200))

