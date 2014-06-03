var http = require("http")
var fs = require("fs")
var ProtoBuf = require("protobufjs")

// raw varint size
function computeRawVarint32Size (value) {
  if ((value & (0xffffffff <<  7)) == 0) return 1
  if ((value & (0xffffffff << 14)) == 0) return 2
  if ((value & (0xffffffff << 21)) == 0) return 3
  if ((value & (0xffffffff << 28)) == 0) return 4
  return 5
}

function writeRawVarint32(value) {
  var size = computeRawVarint32Size(value)
  var buffer = new Buffer(size)

  var i = 0
  while (true) {
    if ((value & ~0x7F) == 0) {
      buffer.writeUInt8(value, i)
      break
    } else {
      var temp = ((value & 0x7F) | 0x80)
      buffer.writeUInt8(temp, i)
      value >>>= 7
    }
    i++
  }
  return buffer
}

function merge(a, b) {
  return Buffer.concat([a, b], a.length + b.length)
}

function formFrame(buffer) {
  var header = writeRawVarint32(buffer.length)
  return merge(header, buffer)
}

var net = require('net')

var socket = new net.Socket()

socket.connect(8080)

var Request = ProtoBuf.protoFromFile("/home/tienson/codes/clojure/clj-netty/resources/proto/rpc.proto").build("Request")

var request = new Request({
  "type": 0,
  "service": "redis",
  "method": "get",
  "args": "bingo"
})

var buffer = request.encode().toBuffer()

var buffer = formFrame(buffer)
// console.log(buffer.length)
socket.write(buffer)

socket.on('data', function(data) {
  console.log(data.toString('utf-8'))
})
