# pip install fpdf

from fpdf import FPDF

pdf = FPDF()
pdf.add_page()
pdf.set_font("Arial", size=12)
pdf.cell(200, 10, txt="John was born on March 7th, 1941.", ln=True)
pdf.output("john.pdf")

print("PDF created successfully: small.pdf")