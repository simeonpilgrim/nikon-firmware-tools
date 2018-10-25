from idautils import *
from idc import *



class IntervalMap:
	def __init__(self):
		self.vals = []
		self.count = 0

	def insert(self, start, end):
		if self.count == 0:
			# insert first
			self.vals = [[start,end]]
			self.count += 1
			return

		idx = 0
		for p in self.vals:
			ps = p[0]
			pe = p[1]
			if pe < start: # go to next	
				if idx+1 == self.count: # there is no next insert here
					self.vals.insert(idx + 1, [start,end])
					self.count += 1
					return
			elif end < ps: # insert before idx
				self.vals.insert(idx, [start,end])
				self.count += 1
				return
			elif pe == start:
				p[1] = end
				if idx+1 < self.count and self.vals[idx+1][0] == end:
					p[1] = self.vals[idx+1][1]
					del self.vals[idx+1]
					self.count -= 1
				return
			elif ps == end:
				p[0] = start
				return
			idx += 1

	def inMap(self, value):
		for p in self.vals:
			if value < p[0]: 
				return False
			if value >= p[0] and value < p[1]:
				return True
		return False

	def showMap(self):
		for p in self.vals:
			print "0x%x-0x%x" %(p[0], p[1])
 

def get_string(addr):
	out = ""
	while True:
		if Byte(addr) != 0:
			out += chr(Byte(addr))
		else:
			break
		addr += 1
	return out


class RttiObj:
	def __init__(self, addr, name, parents):
		self.addr = addr
		self.parents = parents
		self.type = type

		if name[0].isdigit() and name[1].isalpha():
			self.name = name[1:len(name)]
		elif name[0].isdigit() and name[1].isdigit() and name[2].isalpha():
			self.name = name[2:len(name)]
		else:
			print "** string name %s" % (name)
			self.name = name

	def __str__(self):
		return "rttiobj 0x%x %s" % (self.addr,self.name)

rtti_objects = {}
rtti_obj_mem_map = IntervalMap()


def load_rtti_object(addr):
	obj = rtti_objects.get(addr)
	if not obj:
		type = Dword(addr)
		print "rtti Addr: 0x%x value: 0x%x" % (addr, type)
		if type == 0x912B5A2C:
			print "leaf class"
			name_ref = Dword(addr + 4)
			name = get_string(name_ref);
			#print "Name Addr: 0x%x value: %s" % (name_ref, name)
			obj = RttiObj(addr, name, {})
			rtti_objects[addr] = obj
			rtti_obj_mem_map.insert(addr, addr + 8)
			
		elif type == 0x912B5A3C:
			print "simple class"
			name_ref = Dword(addr + 4)
			name = get_string(name_ref);
			print "Name Addr: 0x%x value: %s" % (name_ref, name)
			parent_addr = Dword(addr + 8)
			print "Parents Addr: 0x%x" % (parent_addr)
			parent = load_rtti_object(parent_addr)
			obj = RttiObj(addr, name, {0x0:parent})
			rtti_objects[addr] = obj
			rtti_obj_mem_map.insert(addr, addr + 12)
			
		else:
			print "** type unknown Addr: 0x%x type: 0x%x" % (addr, type)

	return obj

rtti_vtab_mem_map = IntervalMap()

class VtableObj:
	def __init__(self, addr, name):
		self.addr = addr
		self.name = name


def process_func(func_addr, addr, vtab_func_idx):
    vtab_addr = Dword(addr)
    func_addr = func_addr & 0xFFffFFfe
    #print "func 0x%x vtab 0x%x idx 0x%x" % (func_addr, addr, vtab_func_idx)
    obj = rtti_objects.get(vtab_addr)
    name = "%s_0x%x" % (obj.name,vtab_func_idx)
    #print name
    MakeNameEx(func_addr, name, 0)

def load_vtable(addr):
	class_addr = Dword(addr)
	print "vtable Addr: 0x%x" % (addr)
	
	if class_addr == 0: 
		print "Addr 0x%x not vtable" % (addr)
		return addr + 4

	offset = 4
	vtable_offset = 0
	func_idx = 0
	vtable_class = load_rtti_object(class_addr)


	looping = 1
	while looping:
		value = idaapi.as_uint32(Dword(addr + offset))
		if value == 0:
			rtti_vtab_mem_map.insert(addr, addr + offset + 4)
			return addr + offset + 4
		elif addr + offset == end_addr:
			rtti_vtab_mem_map.insert(addr, addr + offset)
			return addr + offset            
		elif value > 0:
			print " Addr 0x%x Offset %d Func [%d] 0x%x" % (addr, vtable_offset, func_idx, value)
			process_func(value,addr,func_idx)
			func_idx += 1
		else:
			print "multi inheritance needs sorting out..."
			quit()
		offset += 4

# def scan_vtables(start, end):
	# addr = start
	# while addr < end:
		# addr = load_vtable(addr)


def follow_rtti_names(start, end, srch):
	addr = start
	rtti_names = {}
	while addr < end:
		idc.MakeStr(addr, idc.BADADDR)
		str = idc.GetString(addr)
		slen = len(str)
		rtti_names[addr] = str
		addr = addr + slen + 1
	
	srch_code = idc.SEARCH_DOWN + idc.SEARCH_CASE + idc.SEARCH_NOSHOW 
	ea = idc.FindBinary(start_addr, srch_code, srch)
	last = 0
	while ea != idc.BADADDR and ea != last:
		last = ea;
		#print " ea 0x%x" %ea
		real_ea = ea - 2
		val = Dword(real_ea)
		token = rtti_names.get(val)
		if token:
			print "found 0x%x 0x%x %s" % (real_ea,val,rtti_names[val])
			load_rtti_object(real_ea - 4)
		ea = idc.FindBinary(ea + 4, srch_code, srch)





print "[*]"
print "[*] Attempting to decode rtti table"
start_addr = 0x91020000;
end_addr = 0x912B600C; # beging of static C string table

# function 9027CFE6 is Pure_virtua_fn

follow_rtti_names(0x912B53B0, 0x912B5A23, "912B")

print "object mem map:"
rtti_obj_mem_map.showMap()

for k,r in rtti_objects.items():
	idc.MakeName(r.addr, "O_" + r.name)

vtab_possibles = set()

# find all references to the objects
srch = "? ? ? 91" # based off mem_map range above
srch_code = idc.SEARCH_DOWN + idc.SEARCH_CASE + idc.SEARCH_NOSHOW 
ea = idc.FindBinary(start_addr, srch_code, srch)
last = 0
while ea != idc.BADADDR and ea != last:
	last = ea;
	#print " ea 0x%x" %ea
	real_ea = ea
	val = Dword(real_ea)
	#print " ea: 0x%x val: 0x%x" %(real_ea, val)
	if (real_ea & 3) == 0:	
		obj_ref = rtti_objects.get(val)
		if obj_ref and rtti_obj_mem_map.inMap(real_ea) == False:
			print "found 0x%x 0x%x %s" % (real_ea,val,rtti_objects[val])
			vtab_possibles.add(real_ea)
	ea = idc.FindBinary(ea + 4, srch_code, srch)

for v in sorted(vtab_possibles):
	if rtti_vtab_mem_map.inMap(v) == False:
		print "possible vtable 0x%x" % (v)
		load_vtable(v)


