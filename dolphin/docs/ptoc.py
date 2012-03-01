# ptoc.py - print the table of contents of a latex document
#!/usr/bin/env python
import sys

#[ constants
INDENT='  '
APPENDIX_STR='Appendix:'
#] constants

class Section:
   def __init__(self, control_name):
      self.control_name=control_name
      self.number=1
   def get_control_str(self):
      return '\\'+self.control_name

sections=[
   #Section("part"),
   Section("chapter"),
   Section("section"),
   Section("subsection"),
   Section("subsubsection"),
   Section("paragraph"),
   Section("subparagraph")
]

if len(sys.argv) <= 1 or sys.argv[1][-4:]!='.tex':
   print 'please specify a latex file(*.tex)'
   exit()

filename=sys.argv[1]

try:
   f=open(filename, 'r')
   for line in f:
      sline=line.strip()
      if sline == '' or sline.find('%')==0: 
         continue
      for secIndex in range(len(sections)):
         sec=sections[secIndex]
         if sline.find(sec.get_control_str())>=0:
            sys.stdout.write(INDENT*secIndex)
            titleStart=sline.find('{')+1
            #print sec.name,
            for i in range(0, secIndex):
               sys.stdout.write(str(sections[i].number-1)+'.')
            print sec.number,
            print sline[ titleStart: sline.find('}', titleStart+1) ]
            sec.number+=1
            #[ reset children
            for childIndex in range(secIndex+1, len(sections)):
               sections[childIndex].number=1
            break
         elif sline.find('\\appendix')>=0:
            print APPENDIX_STR
            break
      #print line
except IOError:
   print 'read error'
finally:
   f.close()

