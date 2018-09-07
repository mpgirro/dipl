
quartzFonts(cmu_sans = c("CMU Sans Serif", "CMU Sans Serif Bold", "CMU Sans Serif Oblique", "CMU Sans Serif BoldOblique"))
#quartzFonts(cmu_serif = c("CMU Serif Roman", "CMU Serif Bold", "CMU Serif Italic", "CMU Serif BoldItalic"))

library(extrafont)
font_import(pattern = "CM")
loadfonts()
#par(family = "CM Sans")

index_cpumem <- read.table("data/index-cpumem.csv", header=T, sep=";");

# this function plots the graphic of the data
eval_mem <- function() {
  par(mar=c(8,4,1,1))
  end_point = 0.5 + nrow(index_cpumem) + nrow(index_cpumem)-1 #this is the line which does the trick (together with barplot "space = 1" parameter)
  barplot(index_cpumem$mem, 
          ylim = c(0, 175), ylab = "Memory Usage [Megabytes]", las=2,
          #space=c(1, 2),
          space=c(1,0.6),
          col=c("tomato1","dodgerblue1","dodgerblue1","dodgerblue1","dodgerblue1","dodgerblue1","dodgerblue1","dodgerblue1"),
          cex.axis = 1.2, cex.lab = 1.2
          )
  #rotate 60 degrees, srt=60
  text(seq(1.5,end_point,by=2), par("usr")[3]-0.25, 
       srt = 60, adj= 1.1, xpd = TRUE,
       labels = as.character(index_cpumem$artefact), cex=1.2)
}

# execute this to show in Rstudio
par(family = "cmu_sans")
eval_mem()


dest <- "out/eval-index-mem"

# output PNG
png(paste(dest, ".png", sep=""), width = 5, height = 5, units = 'in', res = 300)
par(family = "cmu_sans")
eval_mem()
dev.off()

# output PDF
pdf(paste(dest, ".pdf", sep=""), family="CM Sans", width = 5, height = 5)
par(family = "CM Sans")
eval_mem()
dev.off()
# embed the CM Sans font into the PDF, or printing might become a problem
embed_fonts(paste(dest, ".pdf", sep=""), outfile=paste(dest, "_embed.pdf", sep=""))

