
quartzFonts(cmu_sans = c("CMU Sans Serif", "CMU Sans Serif Bold", "CMU Sans Serif Oblique", "CMU Sans Serif BoldOblique"))
#quartzFonts(cmu_serif = c("CMU Serif Roman", "CMU Serif Bold", "CMU Serif Italic", "CMU Serif BoldItalic"))

library(extrafont)
font_import(pattern = "CM")
loadfonts()
#par(family = "CM Sans")


search_akka_delegation_data <- read.table("data/search-akka-delegation-rtt-overall.csv", header=T, sep=";")
search_akka_future_data <- read.table("data/search-akka-future-rtt-overall.csv", header=T, sep=";")
search_msa_data <- read.table("data/search-msa-rtt-overall.csv", header=T, sep=";")

# milliseconds -> seconds
search_akka_delegation_data$overallRT = search_akka_delegation_data$overallRT / 1000
search_akka_future_data$overallRT = search_akka_future_data$overallRT / 1000
search_msa_data$overallRT = search_msa_data$overallRT / 1000

# aggregate the values for every X, so we'll just show the means of them
a <- aggregate(search_akka_delegation_data$overallRT, list(input_size=search_akka_delegation_data$input_size), mean)
b <- aggregate(search_akka_future_data$overallRT, list(input_size=search_akka_future_data$input_size), mean)
c <- aggregate(search_msa_data$overallRT, list(input_size=search_msa_data$input_size), mean)

# this functions plots the graphic of the data
eval_search <- function() {
  par(mar=c(4,4,1,1))
  plot(c$input_size, c$x, 
       type="l", pch=18, col="dodgerblue1", lty=2, lwd=3,
       cex.axis = 1.3, cex.lab = 1.3,
       xlab = "Search Requests", ylab = "Overall Runtime [Seconds]",
       #xlab="", ylab="",
       xlim=c(0,3000), ylim=c(0, 150), las=1)
  #xaxt="n", yaxt="n")
  lines(a$input_size, a$x, pch=19, col="tomato1", type="l", lty=1, lwd=3)	
  #lines(b$input_size, b$x, pch=19, col="tomato1", type="l", lty=4, lwd=2)	
  legend("topleft", legend=c("Akka", "MSA"),
         col=c("tomato1", "dodgerblue1"), lty=1:2, cex=1.3, lwd=3)
}

eval_akka_delegation_future <- function() {
  par(mar=c(4,4,1,1))
  plot(b$input_size, b$x, 
       type="l", pch=18, col="forestgreen", lty=4, lwd=3,
       cex.axis = 1.3, cex.lab = 1.3,
       xlab = "Search Requests", ylab = "Overall Runtime [Seconds]",
       #xlab="", ylab="",
       xlim=c(0,3000), ylim=c(0, 20), las=1)
  #xaxt="n", yaxt="n")
  lines(a$input_size, a$x, pch=19, col="tomato1", type="l", lwd=3)	
  legend("topleft", legend=c("Akka (Delegation)", "Akka (Future)"),
         col=c("tomato1", "forestgreen"), lty=1:4, cex=1.3, lwd=2)
}

save_eval_akka_delegation_future_images <- function() {
  dest <- "out/eval-search-comparison-akka-delegation-future"
  
  # output PNG
  png(paste(dest, ".png", sep=""), width = 5, height = 5, units = 'in', res = 300)
  par(family = "cmu_sans")
  eval_akka_delegation_future()
  dev.off()
  
  # output PDF
  pdf(paste(dest, ".pdf", sep=""), family="CM Sans", width = 5, height = 5)
  par(family = "CM Sans")
  eval_akka_delegation_future()
  dev.off()
  # embed the CM Sans font into the PDF, or printing might become a problem
  embed_fonts(paste(dest, ".pdf", sep=""), outfile=paste(dest, "_embed.pdf", sep=""))
}

save_eval_search_images <- function() {
  dest <- "out/eval-search-rtt-overall"
  
  # output PNG
  png(paste(dest, ".png", sep=""), width = 5, height = 5, units = 'in', res = 300)
  par(family = "cmu_sans")
  eval_search()
  dev.off()
  
  # output PDF
  pdf(paste(dest, ".pdf", sep=""), family="CM Sans", width = 5, height = 5)
  par(family = "CM Sans")
  eval_search()
  dev.off()
  # embed the CM Sans font into the PDF, or printing might become a problem
  embed_fonts(paste(dest, ".pdf", sep=""), outfile=paste(dest, "_embed.pdf", sep=""))
}


# execute this to show in Rstudio
par(family = "cmu_sans")
eval_search()


par(family = "cmu_sans")
eval_akka_delegation_future()



par(family = "cmu_sans")
save_eval_search_images()

par(family = "cmu_sans")
save_eval_akka_delegation_future_images()

